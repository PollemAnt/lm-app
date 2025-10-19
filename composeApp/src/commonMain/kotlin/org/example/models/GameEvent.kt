package org.example.models

import kotlinx.serialization.Serializable
import org.example.Card

@Serializable
data class GameEvent(
    val type: String,
    val player: Player? = null,
    val activePlayerId: Int? = null,
    val card: Card? = null
)