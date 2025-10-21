package org.example.project.viewModel


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.ApiService
import org.example.Card
import org.example.models.CardPlayedEvent
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.TurnChangedEvent

class GameViewModel(private val api: ApiService, private val scope: CoroutineScope){

    private val _player = MutableStateFlow<Player?>(null)
    val player: StateFlow<Player?> = _player

    private val _gameState = MutableStateFlow<GameSnapshot?>(null)
    val gameState: StateFlow<GameSnapshot?> = _gameState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isConnected = MutableStateFlow<String?>(null)
    val isConnected: StateFlow<String?> = _isConnected

    init {
        scope.launch {
            _isConnected.value = api.ping()
        }
    }

    suspend fun connect() {
        api.connectToUpdates { event ->
            scope.launch {
                when (event) {

                    is CardPlayedEvent -> {
                        _gameState.value = api.getState()
                        println("DRAW CARD")
                    }

                    is PlayerJoinEvent -> {
                        println("PLAYER JOINED")
                        _gameState.value = api.getState()
                    }

                    is TurnChangedEvent -> {
                        _gameState.value = api.getState()
                    }
                }
            }
        }
    }

    fun reset() {
        scope.launch { api.getReset() }
    }

    fun join(name: String) {
        scope.launch {
            _player.value = api.join(name)
            _gameState.value = api.getState()
        }
    }

    fun makeMove(card: Card, opponent: Player) {
        scope.launch {
            val p = _player.value ?: return@launch
            val res = api.makeMove(p.id, card, opponent.id)
            _message.value = res.ok ?: res.error
            _gameState.value = api.getState()
        }
    }

    fun drawCard() {
        scope.launch {
            val p = _player.value ?: return@launch
            val card = api.drawCard(p.id)
            _message.value = "Dobrałeś kartę: ${card.name}"
            val hand = api.getHand(p.id)
            _player.value = p.copy(hand = hand)
        }
    }
}