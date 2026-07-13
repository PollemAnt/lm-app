package org.example.project.UI.screens.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.CardPlayedFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.GameException
import org.example.NetworkClient
import org.example.models.Card
import org.example.models.CardPlayedEvent
import org.example.models.CardType.CHANCELLOR
import org.example.models.CardType.PRIEST
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.RoundEndedEvent
import org.example.models.RoundSummary
import org.example.models.TurnChangedEvent
import org.example.project.UI.screens.viewModel.GameUiEvent.ShowMessage
import org.example.project.UI.screens.viewModel.GameUiEvent.ShowPriestReveal
import org.example.project.UI.screens.viewModel.GameUiEvent.ShowRoundSummary
import org.example.repository.GameRepository
import org.example.repository.api.GameApi

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
    val isLoading: Boolean = false,
    val chancellorState: ChancellorUIState? = null
)


class GameViewModel(private val repository: GameRepository) : ViewModel() {
    constructor() : this(
        GameRepository(
            GameApi(
                client = NetworkClient.httpClient,
                "https://lm-serwer.onrender.com"//"http://192.168.18.104:8080/"
            )
        )
    )

    private val _uiEvents = MutableSharedFlow<GameUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private fun observeEvents() {
        viewModelScope.launch {
            repository.eventsFlow.collect { event ->

                when (event) {

                    is CardPlayedEvent -> {
                        val message = prepareEventMessage(event)
                        println("DEBUG -CardPlayedEvent: $message")
                        _uiEvents.emit(ShowMessage(message))
                    }

                    is TurnChangedEvent -> {
                        val message = "Tura gracza: ${getPlayerName(event.activePlayerId)}"
                        println("DEBUG -TurnChangedEvent: $message")
                        _uiEvents.emit(ShowMessage(message))
                    }

                    is PlayerJoinEvent -> {
                        val message = "Gracz ${event.player.name} dołączył do gry."
                        println("DEBUG -PlayerJoinEvent: $message")
                        _uiEvents.emit(ShowMessage(message))
                    }

                    is RoundEndedEvent -> {
                        println("DEBUG -RoundEndedEvent")
                        _uiEvents.emit(ShowRoundSummary(event.roundSummary))
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
                        getCardName(it.revealedCardNumber)
                    )
                )
            }
        }

    }

    private fun prepareEventMessage(event: CardPlayedEvent): String {
        val feedback = event.feedback
        return createFeedbackMessage(feedback)
    }

    private fun prepareSummaryRoundMessage(event: RoundEndedEvent): String {
        val winner = event.roundSummary.winnerName
        viewModelScope.launch {
            _uiEvents.emit(ShowRoundSummary(event.roundSummary))
        }
        return "Koniec rundy! $winner wygrał rundę!"
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
        connectToUpdates()
        observePriestReveal()

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

    fun join(name: String, onSuccess: (Player) -> Unit) {
        viewModelScope.launch {
            val player = repository.joinPlayer(name)
            _uiState.update { it.copy(player = player) }
            onSuccess(player)
        }
    }

    fun makeMove(card: Card, target: Player?, guessCardNumber: Int?) {
        viewModelScope.launch(Dispatchers.Default) {
            if (card.cardType == CHANCELLOR) {

                _uiState.update {
                    it.copy(
                        chancellorState = ChancellorUIState(
                            availableCards = onChancellorPlayed(
                                card,
                                target
                            )
                        )
                    )
                }

                return@launch

            } else if (card.cardType == PRIEST && target?.id != _uiState.value.player?.id) {
                _uiState.value.player ?: return@launch
                println("DEBUG - makeMove ShowPriestReveal")
                /* val revealedCard = onPriestPlayed(
                     playerId = currentPlayer.id,
                     targetId = target?.id ?: return@launch
                 )


                 if (revealedCard != null) {
                     _uiEvents.emit(
                         ShowPriestReveal(
                             targetPlayerName = target.name,
                             revealedCard = revealedCard.first()
                         )
                     )
                 }*/

            }
            executeMoveInRepository(card, target, guessCardNumber)
        }
    }

    private suspend fun onChancellorPlayed(
        card: Card,
        player: Player?
    ): List<Card> {
        return repository.onChancellorPlayed(player!!.id, card)
    }

    private suspend fun onPriestPlayed(
        playerId: Int,
        targetId: Int
    ): List<Card>? {

        try {
            return repository.onPriestPlayed(playerId, targetId)
        } catch (e: GameException) {
            println("DEBUG - onPriestPlayed GameException: ${e.message}")
            _uiEvents.emit(ShowMessage(e.message ?: "Błąd"))
        }
        return null
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

    private fun createFeedbackMessage(feedback: CardPlayedFeedback): String {

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
                    }. Zgadywana karta: ${getCardName(feedback.guessedCardNumber)}"
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
                getPlayerName(feedback.targetPlayerId)
            }"

            is CardPlayedFeedback.SpyPlayed -> "Gracz ${getPlayerName(feedback.spyPlayerId)} użył Szpiega."
            is CardPlayedFeedback.ChancellorPlayed -> "Gracz ${getPlayerName(feedback.playerId)} zagrał Kanclerza."
        }
        return message
    }

    fun completeChancellorMove(cardToKeep: Card) {
        viewModelScope.launch {
            val playerId = _uiState.value.player?.id ?: return@launch

            try {
                repository.completeChancellorMove(playerId, cardToKeep, playerId)

                _uiState.update { it.copy(chancellorState = null) }

            } catch (e: Exception) {
                _uiEvents.emit(
                    ShowMessage("Błąd podczas wyboru karty: ${e.message}")
                )
                _uiState.update {
                    it.copy(chancellorState = null)
                }

            }
        }
    }


    fun connectToUpdates() {
        viewModelScope.launch { repository.connectToUpdates(_uiState.value.player?.id ?: 0) }
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

    private fun getCardName(cardNumber: Int): String {
        return when (cardNumber) {
            0 -> "Szpieg"
            2 -> "Kapłan"
            3 -> "Baron"
            4 -> "Służąca"
            5 -> "Książę"
            6 -> "Kanclerz"
            7 -> "Król"
            8 -> "Hrabina"
            9 -> "Księżniczka"
            else -> {
                "Nieznana karta"
            }
        }
    }
}