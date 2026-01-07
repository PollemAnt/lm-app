package org.example.repository.api


import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.models.CardPlayedEvent
import org.example.models.GameEventBase
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.PlayerJoinRequest
import org.example.models.TurnChangedEvent
import org.example.models.Card
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.example.NetworkClient
import org.example.models.GameConfig
import org.example.models.RoundEndedEvent

class GameApi(
    private val client: HttpClient = NetworkClient.httpClient,
    private val baseUrl: String = "https://lm-serwer.onrender.com"
) {

    suspend fun ping(): String {
        return try {
            val response: HttpResponse = client.get("$baseUrl/")
            response.bodyAsText()
        } catch (e: Exception) {
            "brak połączenia $e"
        }
    }

    suspend fun connectToUpdates(onEvent: suspend (GameEventBase) -> Unit) {
        client.webSocket(
            urlString = baseUrl.replace("https", "wss") + "/updates"
        ) {
            println("Połączono z WebSocket!")

            for (frame in incoming) {

                val text = (frame as? Frame.Text)?.readText() ?: "Brak.."
                println("[EVENT] API Odebrano event: $text")
                try {
                    val event = json.decodeFromString<GameEventBase>(text)
                    println("[EVENT] API Event: $event")
                    onEvent(event)

                } catch (e: Exception) {
                    println("[EVENT] API Błąd parsowania eventu: $text  error: $e ")
                }
            }
        }
    }

    val eventModule = SerializersModule {
        polymorphic(GameEventBase::class) {
            subclass(PlayerJoinEvent::class)
            subclass(CardPlayedEvent::class)
            subclass(TurnChangedEvent::class)
            subclass(RoundEndedEvent::class)
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

    suspend fun getPlayerHandForPriest(playerId: Int,targetId: Int): List<Card>? {
        val response = client.post("$baseUrl/getPlayerHandForPriest") {
            contentType(ContentType.Application.Json)
            setBody(PriestRequest(playerId, targetId))
        }

        return response.body()

    }

    suspend fun makeMove(
        playerId: Int,
        card: Card,
        targetPlayerId: Int?,
        guessCardNumber: Int?
    ) {
        val response = client.post("$baseUrl/move") {
            contentType(ContentType.Application.Json)
            setBody(MoveRequest(playerId, card, targetPlayerId, guessCardNumber))
        }
        println("[DEBUG] MoveResponse:  makeMove response: $response")
    }

    suspend fun completeChancellorMove(
        playerId: Int,
        cardToKeep: Card,
        targetPlayerId: Int?,
    ) {
        val response = client.post("$baseUrl/complete_chancellor") {
            contentType(ContentType.Application.Json)
            setBody(MoveRequest(playerId, cardToKeep, targetPlayerId, null))
        }
        println("[DEBUG] MoveResponse: completeChancellorMove response: $response")
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

@Serializable
data class MoveRequest(
    val playerId: Int,
    val card: Card,
    val targetPlayerId: Int?,
    val guessCardNumber: Int?
)

@Serializable
data class DrawRequest(val playerId: Int, val card: Card)

@Serializable
data class PriestRequest(val playerId: Int, val targetId: Int)
