package org.example.project.UI.screens.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.CardPlayedFeedback
import com.example.models.MoveResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.example.NetworkClient
import org.example.models.Card
import org.example.models.CardPlayedEvent
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.TurnChangedEvent
import org.example.repository.GameRepository
import org.example.repository.api.GameApi

@Serializable
data class ChancellorUIState(
    val availableCards: List<Card>
)

data class GameUiState(
    val isConnected: String? = null,
    val message: String? = null,
    val player: Player? = null,
    val hand: List<Card> = emptyList(),
    val gameState: GameSnapshot? = null,
    val isLoading: Boolean = false
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    constructor() : this(
        GameRepository(
            GameApi(
                client = NetworkClient.httpClient,
                "https://lm-serwer.onrender.com"
            )
        )
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _chancellorState = MutableStateFlow<ChancellorUIState?>(null)
    val chancellorState: StateFlow<ChancellorUIState?> = _chancellorState.asStateFlow()

    private fun observeEvents() {
        viewModelScope.launch {
            repository.eventsFlow.collect { event ->
                val message = when (event) {
                    is CardPlayedEvent -> {
                        prepareEventMessage(event)
                    }

                    is TurnChangedEvent -> "Tura gracza: ${getPlayerName(event.activePlayerId)}"
                    is PlayerJoinEvent -> "Gracz ${event.player.name} dołączył do gry."
                }

                _uiState.update { it.copy(message = message) }
            }
        }
    }

    private fun prepareEventMessage(event: CardPlayedEvent): String {
        val playerName = getPlayerName(event.playerId)
        var targetName = getPlayerName(event.targetPlayerId)
        if(targetName == playerName){
            targetName = "siebie"
        }
        val cardName = event.card.name

        return "Gracz $playerName zagrał kartę $cardName na $targetName."
    }

    init {
        viewModelScope.launch {
            repository.snapshotFlow.collect { snapshot ->

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
        println("[EVENT] ViewModel: init started")
        observeEvents()
        connectToUpdates()
        println("[EVENT] ViewModel: observeEvents called")
        viewModelScope.launch {
            repository.ping()


        }

        viewModelScope.launch {
            repository.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }

        }
    }

    fun loadInitialState() {
        viewModelScope.launch {
            repository.loadInitialState()
        }
    }

    fun join(name: String) {
        viewModelScope.launch {
            val player = repository.joinPlayer(name)
            _uiState.update { it.copy(player = player) }
        }
    }

    fun makeMove(card: Card, target: Player?, guessCardNumber: Int?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (card.number == 6) {
                println("[DEBUG] ViewModel: Making move for Chancellor")
                _chancellorState.value =
                    ChancellorUIState(availableCards = onChancellorPlayed(card, target))
                println("[DEBUG] _chancellorState.value : ${chancellorState.value}")
                return@launch
            }
            val result = executeMoveInRepository(card, target, guessCardNumber) ?: return@launch

            withContext(Dispatchers.Main) {
                processMoveResult(result)
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
    ): MoveResult? {
        val playerId = _uiState.value.player?.id ?: return null
        val targetId = target?.id
        return repository.makeMove(playerId, card, targetId, guessCardNumber)
    }

    private fun processMoveResult(result: MoveResult) {
        when (result) {
            is MoveResult.Success -> handleMoveSuccess(result)
            is MoveResult.Error -> handleMoveError(result)
        }
    }

    private fun handleMoveSuccess(result: MoveResult.Success) {
        println("[DEBUG] Received feedback: ${result.feedback}")
        _chancellorState.value = null
        handleMoveFeedback(result.feedback)
    }

    private fun handleMoveError(result: MoveResult.Error) {
        _uiState.update { it.copy(message = result.message) }
        _chancellorState.value = null
    }

    private fun handleMoveFeedback(feedback: CardPlayedFeedback) {
        val message = when (feedback) {
            is CardPlayedFeedback.Standard -> feedback.message
            is CardPlayedFeedback.GuardPlayed -> {
                if (feedback.wasCorrect) {
                    "Gracz ${getPlayerName(feedback.guessingPlayerId)} poprawnie odgadł kartę! Gracz ${
                        getPlayerName(
                            feedback.targetPlayerId
                        )
                    } odpada z gry."
                } else {
                    "Gracz ${getPlayerName(feedback.guessingPlayerId)} nie odgadł karty gracza ${
                        getPlayerName(
                            feedback.targetPlayerId
                        )
                    }."
                }
            }

            is CardPlayedFeedback.BaronPlayed -> {
                when {
                    feedback.winnerId != null && feedback.loserId != null ->
                        "Gracz ${getPlayerName(feedback.winnerId)} wygrał pojedynek! Gracz ${
                            getPlayerName(
                                feedback.loserId
                            )
                        } odpada z gry."

                    else -> "Pojedynek Barona zakończył się remisem."
                }
            }

            is CardPlayedFeedback.HandmaidPlayed -> "Gracz ${getPlayerName(feedback.protectedPlayerId)} jest teraz chroniony."
            is CardPlayedFeedback.KingPlayed -> "Gracz ${getPlayerName(feedback.player1Id)} zamienił się kartami z graczem ${
                getPlayerName(
                    feedback.player2Id
                )
            }."

            is CardPlayedFeedback.PrincePlayed -> "Gracz ${getPlayerName(feedback.playerId)} zmusił gracza ${
                getPlayerName(
                    feedback.targetId
                )
            } do odrzucenia karty."

            is CardPlayedFeedback.PriestPlayed -> "Gracz ${getPlayerName(feedback.viewingPlayerId)} podejrzał kartę gracza ${
                getPlayerName(
                    feedback.targetPlayerId
                )
            }: ${feedback.revealedCardNumber}"

            is CardPlayedFeedback.SpyPlayed -> "Gracz ${getPlayerName(feedback.spyPlayerId)} użył Szpiega."
            else -> {
                println("[DEBUG] Otrzymano nieobsługiwany feedback: $feedback")
                "Otrzymano nieznany rezultat ruchu."
            }
        }
        _uiState.update { it.copy(message = message) }
    }


    fun completeChancellorMove(cardToKeep: Card) {
        viewModelScope.launch {
            val playerId = _uiState.value.player?.id ?: return@launch

            try {
                println("[DEBUG] Chancellor completeChancellorMove cardToKeep: $cardToKeep")

                val result = repository.completeChancellorMove(playerId, cardToKeep, playerId)

                println("[DEBUG] Chancellor completeChancellorMove result: $result")
                when (result) {
                    is MoveResult.Success -> {
                        _uiState.update { it.copy(message = result.messageToAll) }
                    }

                    is MoveResult.Error -> {
                        _uiState.update { it.copy(message = result.message) }
                    }
                }

                _chancellorState.value = null
                repository.refreshSnapshot()

            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Błąd podczas wyboru karty: ${e.message}") }
                _chancellorState.value = null
            }
        }
    }

    fun connectToUpdates() {
        println("[EVENT] ViewModel: connectToUpdates called")
        viewModelScope.launch { repository.connectToUpdates() }
    }

    fun reset() {
        viewModelScope.launch {
            repository.reset()
            _uiState.value = GameUiState()
            repository.ping()
        }
    }

    private fun getPlayerName(playerId: Int): String {
        val player = _uiState.value.gameState?.players?.find { it.id == playerId }
        return player?.name ?: "Nieznany gracz"
    }
}