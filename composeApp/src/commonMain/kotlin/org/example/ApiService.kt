package org.example


import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.ws
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.models.GameEvent
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinRequest

class ApiService(private val baseUrl: String = "http://10.0.2.2:8080") {

    private val client = NetworkClient.httpClient

    suspend fun ping(): String {
        return try {
            val response: HttpResponse  = client.get("$baseUrl/")
            response.bodyAsText()
        } catch (e: Exception) {
            "brak połączenia $e"
        }
    }

    suspend fun connectToUpdates(onEvent : (GameEvent) -> Unit){
        client.webSocket(
            urlString = baseUrl.replace("http", "ws") + "/updates"
        ){
            println("Połączono z WebSocket!")

            for(frame in incoming){
                val text = (frame as? Frame.Text)?.readText() ?: ""
                try{
                    val event = Json.decodeFromString<GameEvent>(text)
                    onEvent(event)

                }catch (e: Exception) {
                    println("Błąd parsowania eventu: $text   error: $e ")
                }
            }
        }
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

    suspend fun makeMove(playerId: Int): MoveResult {
        return client.post("$baseUrl/move") {
            contentType(ContentType.Application.Json)
            setBody(MoveRequest(playerId))
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
data class MoveRequest(val playerId: Int)

@Serializable
data class MoveResult(
    val ok: String? = null,
    val error: String? = null,
    val nextPlayerId: Int? = null
)

@Serializable
data class DrawRequest(val playerId: Int)

@Serializable
data class Card(
    val id: Int,
    val number: Int,
    val name: String,
    val description: String
)