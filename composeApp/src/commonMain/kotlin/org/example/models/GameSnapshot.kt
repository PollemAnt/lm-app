package org.example.models

import kotlinx.serialization.Serializable
import org.example.Card

@Serializable
data class GameSnapshot(
    val players: List<Player>,
    val cards: List<Card>,
    val activePlayerId: Int?
)