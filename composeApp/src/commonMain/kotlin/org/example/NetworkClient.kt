package org.example

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.example.shared.protocol.CardPlayedFeedback
import org.example.shared.protocol.ErrorResponse
import org.example.shared.protocol.MoveResult
import org.example.shared.protocol.ServerEvent
import org.example.shared.protocol.PlayerJoinEvent
import org.example.shared.protocol.CardPlayedEvent
import org.example.shared.protocol.TurnChangedEvent
import org.example.shared.protocol.RoundEndedEvent
import org.example.shared.protocol.GameSnapshotEvent
import org.example.shared.protocol.PriestRevealEvent
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage

class GameException(message: String) : Exception(message)

object NetworkClient {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
        classDiscriminator = "type"

        serializersModule = SerializersModule {
            polymorphic(CardPlayedFeedback::class) {
                subclass(CardPlayedFeedback.Standard::class)
                subclass(CardPlayedFeedback.GuardPlayed::class)
                subclass(CardPlayedFeedback.BaronPlayed::class)
                subclass(CardPlayedFeedback.HandmaidPlayed::class)
                subclass(CardPlayedFeedback.KingPlayed::class)
                subclass(CardPlayedFeedback.PrincePlayed::class)
                subclass(CardPlayedFeedback.PriestPlayed::class)
                subclass(CardPlayedFeedback.SpyPlayed::class)
                subclass(CardPlayedFeedback.ChancellorPlayed::class)
            }
            polymorphic(MoveResult::class) {
                subclass(MoveResult.Success::class)
                subclass(MoveResult.Error::class)
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
    }

    val httpClient = HttpClient {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(ContentNegotiation) {
            json(json)
        }

        expectSuccess = true

        HttpResponseValidator {

            handleResponseExceptionWithRequest { exception, _ ->

                when (exception) {

                    is ClientRequestException -> {
                        val response = exception.response
                        println("DEBUG - ClientRequestException: ${response.status} ${exception.message}")
                        try {
                            val error = response.body<ErrorResponse>()
                            throw GameException(error.message)
                        } catch (e: Exception) {
                            throw GameException("Błąd ${response.status} ${e.message}")
                        }
                    }

                    is ServerResponseException -> {
                        val response = exception.response
                        try {
                            val error = response.body<ErrorResponse>()
                            throw GameException(error.message)
                        } catch (_: Exception) {
                            throw GameException("Błąd serwera")
                        }
                    }

                    else -> throw exception
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        install(WebSockets) {
            pingIntervalMillis = 10000L // 10 sekund ping do serwera, by podtrzymać połączenie przez NAT/Proxy
        }
    }
}
