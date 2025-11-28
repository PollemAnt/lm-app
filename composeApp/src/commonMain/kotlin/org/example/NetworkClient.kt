package org.example

import com.example.models.CardPlayedFeedback
import com.example.models.MoveResult
import io.ktor.client.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*

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

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }

        install(WebSockets)
    }
}