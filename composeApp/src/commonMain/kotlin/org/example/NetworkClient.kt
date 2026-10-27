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


class GameException(message: String) : Exception(message)

object NetworkClient {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true

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
                }
            })
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

        install(WebSockets)
    }
}