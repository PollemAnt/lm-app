package org.example.repository.api


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

class GameApi(private val baseUrl: String = "https://lm-serwer.onrender.com") {

    private val client = NetworkClient.httpClient

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
                println("Odebrano event: $text")
                try {
                    val event = json.decodeFromString<GameEventBase>(text)
                    println("Event: $event")
                    onEvent(event)

                } catch (e: Exception) {
                    println("Błąd parsowania eventu: $text   error: $e ")
                }
            }
        }
    }

    val eventModule = SerializersModule {
        polymorphic(GameEventBase::class) {
            subclass(PlayerJoinEvent::class)
            subclass(CardPlayedEvent::class)
            subclass(TurnChangedEvent::class)
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

    suspend fun makeMove(playerId: Int, card: Card, targetPlayerId: Int?): MoveResult {
        return client.post("$baseUrl/move") {
            contentType(ContentType.Application.Json)
            setBody(MoveRequest(playerId, card, targetPlayerId))
        }.body()
    }

    suspend fun drawCard(playerId: Int): Card {
        return client.post("$baseUrl/draw") {
            contentType(ContentType.Application.Json)
            setBody(DrawRequest(playerId))
        }.body()
    }

    suspend fun getHand(playerId: Int): List<Card> {
        return client.get("$baseUrl/hand/$playerId").body()
    }

    suspend fun getReset(): HttpStatusCode {
        return client.get("$baseUrl/reset").body()
    }
}

@Serializable
data class MoveRequest(val playerId: Int, val card: Card, val targetPlayerId: Int?)

@Serializable
data class MoveResult(
    val ok: String? = null,
    val error: String? = null,
    val nextPlayerId: Int? = null
)

@Serializable
data class DrawRequest(val playerId: Int)

