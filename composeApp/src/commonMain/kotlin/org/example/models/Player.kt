package org.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(val id: Int, val name: String,val hand: List<Card> = emptyList(), val points: Int = 0 , val playedCards: List<Card> = emptyList())

@Serializable
data class PlayerJoinRequest(val name: String)