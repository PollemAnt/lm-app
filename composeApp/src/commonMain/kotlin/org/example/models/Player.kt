package org.example.models

import kotlinx.serialization.Serializable
import org.example.Card

@Serializable
data class Player(val id: Int, val name: String,val hand: List<Card> = emptyList())

@Serializable
data class PlayerJoinRequest(val name: String)