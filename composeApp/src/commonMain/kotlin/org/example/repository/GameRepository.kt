package org.example.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.models.Card
import org.example.repository.api.GameApi
import org.example.models.CardPlayedEvent
import org.example.models.GameEventBase
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.TurnChangedEvent
import org.example.repository.api.MoveResult

class GameRepository(
    private val api: GameApi
) {
    private val _snapshotFlow = MutableStateFlow<GameSnapshot?>(null)
    val snapshotFlow: StateFlow<GameSnapshot?> = _snapshotFlow.asStateFlow()

    private val _isConnected = MutableStateFlow<String?>(null)
    val isConnected: StateFlow<String?> = _isConnected

    private val _eventsFlow = MutableSharedFlow<GameEventBase>()
    val eventsFlow: SharedFlow<GameEventBase> = _eventsFlow.asSharedFlow()

    suspend fun connectToUpdates() {
        api.connectToUpdates { event ->
            _eventsFlow.tryEmit(event)
            when (event) {
                is CardPlayedEvent,
                is TurnChangedEvent,
                is PlayerJoinEvent -> {

                        refreshSnapshot()

                }
            }
        }
    }

    suspend fun ping() {
        _isConnected.value = api.ping()
    }

    suspend fun loadInitialState() {
        _snapshotFlow.value = api.getState()
    }

    suspend fun joinPlayer(name: String): Player {
        val player = api.join(name)
        refreshSnapshot()
        return player
    }

    suspend fun makeMove(playerId: Int, card: Card, targetId: Int): MoveResult {
        val result = api.makeMove(playerId, card, targetId)
        refreshSnapshot()
        return result
    }

    suspend fun drawCard(playerId: Int): Card {
        val card = api.drawCard(playerId)
        refreshSnapshot()
        return card
    }

    suspend fun refreshSnapshot() {
        _snapshotFlow.value = api.getState()
    }

    suspend fun reset() {
        api.getReset()
        refreshSnapshot()
    }
}