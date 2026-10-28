package org.example.project.ui.screens.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.project.ui.screens.viewModel.GameUiEvent.ShowMessage
import org.example.project.ui.screens.viewModel.GameUiEvent.ShowPriestReveal
import org.example.project.ui.screens.viewModel.GameUiEvent.ShowRoundSummary
import org.example.repository.GameRepository
import org.example.shared.domain.Card
import org.example.shared.domain.CardNameResolver
import org.example.shared.domain.CardType.CHANCELLOR
import org.example.shared.domain.GameConfig
import org.example.shared.domain.GameSnapshot
import org.example.shared.domain.Player
import org.example.shared.domain.RoundSummary
import org.example.shared.protocol.CardPlayedEvent
import org.example.shared.protocol.ErrorResponse
import org.example.shared.protocol.PlayerJoinEvent
import org.example.shared.protocol.RoundEndedEvent
import org.example.shared.protocol.TurnChangedEvent
import org.example.storage.GameStorage

@Serializable
data class ChancellorUIState(
    val availableCards: List<Card>
)

sealed class GameUiEvent {
    data class ShowMessage(val message: String) : GameUiEvent()
    data class ShowRoundSummary(val summary: RoundSummary) : GameUiEvent()
    data class ShowPriestReveal(
        val targetPlayerName: String,
        val revealedCardName: String
    ) : GameUiEvent()
}

data class GameUiState(
    val isConnected: String? = null,
    val player: Player? = null,
    val hand: List<Card> = emptyList(),
    val gameState: GameSnapshot? = null,
    val gameConfig: GameConfig? = null,
    val isLoading: Boolean = false,
    val isRestoringSession: Boolean = false,
    val errorMessage: String? = null,
    val chancellorState: ChancellorUIState? = null
)

class GameViewModel(
    private val repository: GameRepository,
    private val gameStorage: GameStorage
) : ViewModel() {

    fun getSavedPlayerId(): Int? = gameStorage.getPlayerId()

    fun clearSavedSession() {
        gameStorage.clearPlayerId()
    }

    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _uiState = MutableStateFlow(GameUiState(isRestoringSession = gameStorage.getPlayerId() != null))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private fun observeEvents() {
        viewModelScope.launch {
            repository.eventsFlow.collect { event ->
                when (event) {
                    is CardPlayedEvent -> {
                        val message = GameMessageFormatter.format(
                            feedback = event.feedback,
                            getPlayerName = ::getPlayerName,
                            getCardName = CardNameResolver::getCardName
                        )
                        println("DEBUG - CardPlayedEvent: $message")
                        _uiEvents.emit(ShowMessage(message))
                    }

                    is TurnChangedEvent -> {
                        // Ignorujemy komunikat o zmianie tury zgodnie z życzeniem użytkownika
                    }

                    is PlayerJoinEvent -> {
                        val message = "Gracz ${event.player.name} dołączył do gry."
                        println("DEBUG - PlayerJoinEvent: $message")
                        _uiEvents.emit(ShowMessage(message))
                    }

                    is RoundEndedEvent -> {
                        println("DEBUG - RoundEndedEvent")
                        _uiEvents.emit(ShowRoundSummary(event.roundSummary))
                        val winner = event.roundSummary.winnerName
                        _uiEvents.emit(ShowMessage("Koniec rundy! Zwycięzca: $winner"))
                    }
                }
            }
        }
    }

    private fun observePriestReveal() {
        viewModelScope.launch {
            repository.priestReveal.collect {
                println("DEBUG - observePriestReveal")
                _uiEvents.emit(
                    ShowPriestReveal(
                        getPlayerName(it.targetPlayerId),
                        CardNameResolver.getCardName(it.revealedCardNumber)
                    )
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.snapshotFlow.collect { snapshot ->
                println("snapshotFlow.collect")
                _uiState.update { currentState ->
                    val currentPlayer = snapshot?.players?.find { it.id == currentState.player?.id }
                    currentState.copy(
                        gameState = snapshot,
                        player = currentPlayer,
                        hand = currentPlayer?.hand ?: emptyList()
                    )
                }
            }
        }
        observeEvents()
        observePriestReveal()

        viewModelScope.launch {
            repository.ping()
        }

        viewModelScope.launch {
            repository.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }

        viewModelScope.launch {
            try {
                val config = repository.getGameConfig()
                _uiState.update { it.copy(gameConfig = config) }
            } catch (e: Exception) {
                println("Błąd pobierania konfiguracji: $e")
            }
        }

        loadInitialState()
    }

    fun loadInitialState() {
        val playerId = _uiState.value.player?.id
        viewModelScope.launch {
            try {
                repository.loadInitialState(playerId)
            } catch (e: Exception) {
                println("Błąd ładowania stanu gry: $e")
                _uiState.update {
                    it.copy(errorMessage = "Błąd: ${e.message}")
                }
            }
        }
    }

    fun join(name: String, onSuccess: (Player) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, isLoading = true) }
            try {
                val player = repository.joinPlayer(name)
                gameStorage.savePlayerId(player.id)
                _uiState.update { it.copy(player = player, hand = player.hand, isLoading = false) }
                connectToUpdates()
                loadInitialState()
                onSuccess(player)
            } catch (e: ClientRequestException) {
                val errorBody = e.response.body<ErrorResponse>()
                _uiState.update { it.copy(errorMessage = errorBody.message, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "Błąd połączenia: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun rejoin(
        playerId: Int,
        onSuccess: (Player) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null, isRestoringSession = true) }
            try {
                val player = repository.rejoin(playerId)
                gameStorage.savePlayerId(player.id)
                _uiState.update { it.copy(player = player, hand = player.hand, isRestoringSession = false) }
                connectToUpdates()
                loadInitialState()
                onSuccess(player)
            } catch (e: Exception) {
                gameStorage.clearPlayerId()
                _uiState.update {
                    it.copy(
                        errorMessage = "Nie udało się wznowić sesji: ${e.message}",
                        isRestoringSession = false
                    )
                }
                println("Błąd podczas wznawiania: $e")
                onError?.invoke(e)
            }
        }
    }

    fun makeMove(card: Card, target: Player?, guessCardNumber: Int?) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = _uiState.value
            val currentPlayerId = currentState.player?.id ?: return@launch
            val activePlayerId = currentState.gameState?.activePlayerId

            if (activePlayerId != null && activePlayerId != currentPlayerId) {
                _uiEvents.emit(ShowMessage("To nie jest Twoja tura!"))
                return@launch
            }

            try {
                if (card.cardType == CHANCELLOR) {
                    val cards = onChancellorPlayed(card, target)
                    _uiState.update {
                        it.copy(
                            chancellorState = ChancellorUIState(availableCards = cards)
                        )
                    }
                    return@launch
                }
                executeMoveInRepository(card, target, guessCardNumber)
            } catch (e: Exception) {
                println("DEBUG - makeMove Error: ${e.message}")
                _uiEvents.emit(ShowMessage(e.message ?: "Błąd podczas ruchu"))
            }
        }
    }

    private suspend fun onChancellorPlayed(
        card: Card,
        player: Player?
    ): List<Card> {
        return repository.onChancellorPlayed(player!!.id, card)
    }

    private suspend fun executeMoveInRepository(
        card: Card,
        target: Player?,
        guessCardNumber: Int?
    ) {
        val playerId = _uiState.value.player?.id ?: return
        val targetId = target?.id
        return repository.makeMove(playerId, card, targetId, guessCardNumber)
    }

    fun completeChancellorMove(cardToKeep: Card) {
        viewModelScope.launch {
            val playerId = _uiState.value.player?.id ?: return@launch

            try {
                repository.completeChancellorMove(playerId, cardToKeep, playerId)
                _uiState.update { it.copy(chancellorState = null) }
            } catch (e: Exception) {
                println("DEBUG - completeChancellorMove Error: ${e.message}")
                _uiEvents.emit(
                    ShowMessage("Błąd podczas wyboru karty: ${e.message}")
                )
                _uiState.update {
                    it.copy(chancellorState = null)
                }
            }
        }
    }

    private var connectionJob: kotlinx.coroutines.Job? = null

    fun connectToUpdates() {
        val playerId = _uiState.value.player?.id ?: 0
        if (playerId == 0) return

        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            repository.connectToUpdates(playerId)
        }
    }

        fun reset() {
        viewModelScope.launch {
            try {
                gameStorage.clearPlayerId()
                repository.reset()
                _uiState.value = GameUiState()
                repository.ping()
            } catch (e: Exception) {
                println("Błąd resetowania: $e")
                _uiEvents.emit(ShowMessage("Błąd resetowania: ${e.message}"))
            }
        }
    }

    private fun getPlayerName(playerId: Int): String {
        val player = _uiState.value.gameState?.players?.find { it.id == playerId }
        return player?.name ?: "Nieznany gracz"
    }
}
