package org.example.repository.api


import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.example.NetworkClient
import org.example.project.Constants
import org.example.shared.domain.Card
import org.example.shared.domain.GameConfig
import org.example.shared.domain.GameSnapshot
import org.example.shared.domain.Player
import org.example.shared.protocol.CardPlayedEvent
import org.example.shared.protocol.CardPlayedFeedback
import org.example.shared.protocol.DrawRequest
import org.example.shared.protocol.GameSnapshotEvent
import org.example.shared.protocol.MoveRequest
import org.example.shared.protocol.PlayerJoinEvent
import org.example.shared.protocol.PlayerJoinRequest
import org.example.shared.protocol.PriestRequest
import org.example.shared.protocol.PriestRevealEvent
import org.example.shared.protocol.RoundEndedEvent
import org.example.shared.protocol.ServerEvent
import org.example.shared.protocol.TurnChangedEvent

class GameApi(
    private val client: HttpClient = NetworkClient.httpClient,
    private val baseUrl: String = Constants.BASE_URL
) {

    suspend fun ping(): String {
        return try {
            val response: HttpResponse = client.get("$baseUrl/")
            response.bodyAsText()
        } catch (e: Exception) {
            "brak połączenia $e"
        }
    }

    suspend fun connectToUpdates(playerId: Int,onEvent: suspend (ServerEvent) -> Unit) {
        client.webSocket(
            urlString = baseUrl.replace("http", "ws") + "/updates?playerId=$playerId"
        ) {

            println("DEBUG [EVENT] - WebSocket connected to $baseUrl/updates")

            for (frame in incoming) {
                val text = (frame as? Frame.Text)?.readText() ?: continue
                println("DEBUG [WS RECEIVE] Raw text: $text")
                try {
                    val event = json.decodeFromString<ServerEvent>(text)
                    println("DEBUG [WS RECEIVE] Parsed event: ${event::class.simpleName}")
                    onEvent(event)

                } catch (e: Exception) {
                    println("DEBUG [WS ERROR] Failed to parse: $text | Error: $e")
                }
            }
        }
    }

    val eventModule = SerializersModule {
        polymorphic(CardPlayedFeedback::class) {
            subclass(CardPlayedFeedback.Standard::class)
            subclass(CardPlayedFeedback.SpyPlayed::class)
            subclass(CardPlayedFeedback.GuardPlayed::class)
            subclass(CardPlayedFeedback.PriestPlayed::class)
            subclass(CardPlayedFeedback.BaronPlayed::class)
            subclass(CardPlayedFeedback.HandmaidPlayed::class)
            subclass(CardPlayedFeedback.PrincePlayed::class)
            subclass(CardPlayedFeedback.ChancellorPlayed::class)
            subclass(CardPlayedFeedback.KingPlayed::class)
        }
        polymorphic(ServerEvent::class) {
            subclass(PlayerJoinEvent::class)
            subclass(CardPlayedEvent::class)
            subclass(TurnChangedEvent::class)
            subclass(RoundEndedEvent::class)

            subclass(GameSnapshotEvent::class)
            subclass(PriestRevealEvent::class)
        }
    }

    val json = Json {
        serializersModule = eventModule
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun join(name: String): Player {
        return client.post("$baseUrl/join") {
            contentType(ContentType.Application.Json)
            setBody(PlayerJoinRequest(name))
        }.body()
    }

    suspend fun getState(): GameSnapshot {
        return client.get("$baseUrl/state").body()
    }

    suspend fun getPlayer(playerId: Int): Player {
        return client.get("$baseUrl/player/$playerId").body()
    }

    suspend fun getPlayerHandForPriest(playerId: Int,targetId: Int): List<Card> {
        return client.post("$baseUrl/getPlayerHandForPriest") {
            contentType(ContentType.Application.Json)
            setBody(PriestRequest(playerId, targetId))
        }.body()
    }

    suspend fun makeMove(
        playerId: Int,
        card: Card,
        targetPlayerId: Int?,
        guessCardNumber: Int?
    ) {
        client.post("$baseUrl/move") {
            contentType(ContentType.Application.Json)
            setBody(MoveRequest(playerId, card, targetPlayerId, guessCardNumber))
        }
    }

    suspend fun completeChancellorMove(
        playerId: Int,
        cardToKeep: Card,
        targetPlayerId: Int?,
    ) {
        client.post("$baseUrl/complete_chancellor") {
            contentType(ContentType.Application.Json)
            setBody(MoveRequest(playerId, cardToKeep, targetPlayerId, null))
        }
    }


    suspend fun drawCardForChancellor(playerId: Int, chancellorCard: Card): List<Card> {
        val response = client.post("$baseUrl/drawCardForChancellor") {
            contentType(ContentType.Application.Json)
            setBody(DrawRequest(playerId, chancellorCard))
        }

        return response.body()
    }

    suspend fun getReset(): HttpStatusCode {
        return client.get("$baseUrl/reset").body()
    }

    suspend fun updateGameConfig(gameConfig: GameConfig) {
        client.post("$baseUrl/updateGameConfig") {
            contentType(ContentType.Application.Json)
            setBody(gameConfig)
        }
    }

    suspend fun getGameConfig(): GameConfig {
        return client.get("$baseUrl/getGameConfig").body()
    }
}

