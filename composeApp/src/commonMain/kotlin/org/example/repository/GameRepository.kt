package org.example.repository

import kotlinx.coroutines.channels.BufferOverflow
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

class GameRepository(
    private val api: GameApi
) {
    private val _snapshotFlow = MutableStateFlow<GameSnapshot?>(null)
    val snapshotFlow: StateFlow<GameSnapshot?> = _snapshotFlow.asStateFlow()

    private val _isConnected = MutableStateFlow<String?>(null)
    val isConnected: StateFlow<String?> = _isConnected

    private val _eventsFlow = MutableSharedFlow<GameEventBase>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventsFlow: SharedFlow<GameEventBase> = _eventsFlow.asSharedFlow()

    suspend fun connectToUpdates() {
        println("[EVENT] Repository: Starting connectToUpdates")
        try {
            api.connectToUpdates { event ->
                println("[EVENT] Repository: Received event from API: ${event::class.simpleName}")
                _eventsFlow.tryEmit(event)
                println("[EVENT] Repository: try emit: $event")
                when (event) {
                    is CardPlayedEvent,
                    is TurnChangedEvent,
                    is PlayerJoinEvent -> {
                        refreshSnapshot()
                    }
                }
            }
        } catch (e: Exception) {
            println("[EVENT] Repository: WebSocket error: ${e.message}")
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

    suspend fun makeMove(
        playerId: Int,
        card: Card,
        targetId: Int?,
        guessCardNumber: Int?
    ) {
        val result = api.makeMove(playerId, card, targetId, guessCardNumber)
        refreshSnapshot()

    }

    suspend fun completeChancellorMove(playerId: Int, cardToKeep: Card, targetId: Int?) {
        val result = api.completeChancellorMove(playerId, cardToKeep, targetId)
        refreshSnapshot()
    }

    suspend fun refreshSnapshot() {
        _snapshotFlow.value = api.getState()
    }

    suspend fun onPriestPlayed(targetId: Int): List<Card>? {
        return api.getPlayerHand(targetId)
    }

    suspend fun reset() {
        api.getReset()
        refreshSnapshot()
    }

    suspend fun onChancellorPlayed(
        playerId: Int,
        card: Card,
    ): List<Card> {
        return api.drawCardForChancellor(playerId, card)
    }
}