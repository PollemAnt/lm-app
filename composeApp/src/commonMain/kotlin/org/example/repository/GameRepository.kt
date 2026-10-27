package org.example.repository

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.shared.domain.Card
import org.example.repository.api.GameApi
import org.example.shared.domain.GameConfig
import org.example.shared.protocol.GameEventBase
import org.example.shared.domain.GameSnapshot
import org.example.shared.protocol.GameSnapshotEvent
import org.example.shared.domain.Player
import org.example.shared.protocol.PriestRevealEvent

class GameRepository(
    private val api: GameApi
) {
    private val _snapshotFlow = MutableStateFlow<GameSnapshot?>(null)
    val snapshotFlow: StateFlow<GameSnapshot?> = _snapshotFlow.asStateFlow()

    private val _priestReveal = MutableSharedFlow<PriestRevealEvent>()
    val priestReveal = _priestReveal.asSharedFlow()

    private val _isConnected = MutableStateFlow<String?>(null)
    val isConnected: StateFlow<String?> = _isConnected

    private val _eventsFlow = MutableSharedFlow<GameEventBase>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val eventsFlow: SharedFlow<GameEventBase> = _eventsFlow.asSharedFlow()

    suspend fun connectToUpdates(playerId: Int) {
        try {
            api.connectToUpdates(playerId) { event ->
                println("Debug [EVENT] Repository: $event")
                when (event) {

                    is GameSnapshotEvent -> {
                        _snapshotFlow.value = event.snapshot
                    }

                    is GameEventBase -> {
                        _eventsFlow.tryEmit(event)
                    }

                    is PriestRevealEvent -> {
                        _priestReveal.emit(event)
                        println("Repository priestReveal $_priestReveal")
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

    suspend fun loadInitialState(playerId: Int? = null) {
        _snapshotFlow.value = api.getState(playerId)
    }

    suspend fun joinPlayer(name: String): Player {
        val player = api.join(name)
        return player
    }

    suspend fun makeMove(
        playerId: Int,
        card: Card,
        targetId: Int?,
        guessCardNumber: Int?
    ) {
        api.makeMove(playerId, card, targetId, guessCardNumber)
    }

    suspend fun completeChancellorMove(playerId: Int, cardToKeep: Card, targetId: Int?) {
        api.completeChancellorMove(playerId, cardToKeep, targetId)
    }

    suspend fun onPriestPlayed(playerId: Int, targetId: Int): List<Card> {
        return api.getPlayerHandForPriest(playerId, targetId)
    }

    suspend fun reset() {
        api.getReset()
    }

    suspend fun onChancellorPlayed(
        playerId: Int,
        card: Card,
    ): List<Card> {
        return api.drawCardForChancellor(playerId, card)
    }

    suspend fun updateGameConfig(config: GameConfig) {
        api.updateGameConfig(config)
    }

    suspend fun getGameConfig(): GameConfig {
        return api.getGameConfig()
    }
}