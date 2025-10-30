package org.example.project.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.models.Card
import org.example.repository.GameRepository
import org.example.models.CardPlayedEvent
import org.example.models.GameEventBase
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.TurnChangedEvent
import org.example.repository.api.GameApi

data class GameUiState(
    val isConnected: String? = null,
    val message: String? = null,
    val player: Player? = null,
    val gameState: GameSnapshot? = null,
    val isLoading: Boolean = false
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    constructor() : this(GameRepository(GameApi("https://lm-serwer.onrender.com")))

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Obserwuj stan gry
        viewModelScope.launch {
            repository.snapshotFlow.collect { snapshot ->
                _uiState.update { it.copy(gameState = snapshot) }
                _uiState.update { it.copy(player = snapshot?.players?.find { it.id == _uiState.value.player?.id }) }
            }
        }

        // Obserwuj eventy z serwera
        viewModelScope.launch {
            repository.eventsFlow.collect { event ->
                handleEvent(event)
            }
        }

        viewModelScope.launch {
            repository.ping()
        }

        viewModelScope.launch {
            repository.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }

        }
    }

    private suspend fun handleEvent(event: GameEventBase) {
        when (event) {
            is PlayerJoinEvent -> {
                _uiState.update {
                    it.copy(message = "Dołączył gracz: ${event.player.name}")
                }
                repository.refreshSnapshot()
            }

            is CardPlayedEvent -> {
                repository.refreshSnapshot()
            }

            is TurnChangedEvent -> {
                repository.refreshSnapshot()
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

    fun makeMove(card: Card, target: Player) {
        viewModelScope.launch {
            val gameState = _uiState.value.gameState ?: return@launch
            val result = repository.makeMove(
                gameState.players.find { it.id == _uiState.value.player?.id }?.id ?: -1,
                card,
                target.id
            )
            val message = result.ok ?: result.error ?: "Błąd wykonania ruchu"
            _uiState.update { it.copy(message = message) }
        }
    }

    fun drawCard() {
        viewModelScope.launch {
            val player = _uiState.value.player ?: return@launch
            val card = repository.drawCard(player.id)
            _uiState.update { it.copy(message = "Dobrałeś kartę: ${card.name}") }
        }
    }

    fun connectToUpdates() {
        viewModelScope.launch { repository.connectToUpdates() }
    }

    fun reset() {
        viewModelScope.launch {
            repository.reset()
            _uiState.value = GameUiState()
        }
    }


}