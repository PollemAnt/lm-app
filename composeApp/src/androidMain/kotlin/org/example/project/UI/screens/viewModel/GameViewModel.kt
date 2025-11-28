package org.example.project.UI.screens.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.CardPlayedFeedback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

data class PriestRevealState(
    val isRevealed: Boolean = false,
    val targetPlayerName: String = "",
    val revealedCard: Card? = null
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

    private val _priestRevealState = MutableStateFlow<PriestRevealState?>(null)
    val priestRevealState: StateFlow<PriestRevealState?> = _priestRevealState.asStateFlow()

    fun dismissPriestReveal() {
        _priestRevealState.value = PriestRevealState(isRevealed = false)
    }

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
        val feedback = event.feedback
        return createFeedbackMessage(feedback)
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
            } else if (card.number == 2 && target?.id != _uiState.value.player?.id) {
                val revealedCard = onPriestPlayed(targetId = target?.id ?: return@launch)

                if (revealedCard != null) {
                    _priestRevealState.value = PriestRevealState(
                        isRevealed = true,
                        targetPlayerName = target.name,
                        revealedCard = revealedCard.first()
                    )
                    println("[VM] Ujawniona karta: ${revealedCard.first().name}. Aktualizacja UI.")
                }
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
        targetId: Int
    ): List<Card>? {
        return repository.onPriestPlayed(targetId)
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
                getPlayerName(
                    feedback.targetPlayerId
                )
            }: ${feedback.revealedCardNumber}"

            is CardPlayedFeedback.SpyPlayed -> "Gracz ${getPlayerName(feedback.spyPlayerId)} użył Szpiega."
            is CardPlayedFeedback.ChancellorPlayed -> "Gracz ${getPlayerName(feedback.playerId)} zagrał Kanclerza."
        }
        return message
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


    fun completeChancellorMove(cardToKeep: Card) {
        viewModelScope.launch {
            val playerId = _uiState.value.player?.id ?: return@launch

            try {
                println("[DEBUG] Chancellor completeChancellorMove cardToKeep: $cardToKeep")

                repository.completeChancellorMove(playerId, cardToKeep, playerId)

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