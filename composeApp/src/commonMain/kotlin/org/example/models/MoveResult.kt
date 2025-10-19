package org.example.models

import kotlinx.serialization.Serializable

@Serializable
data class MoveResult(val ok: String? = null, val error: String? = null, val nextPlayerId: Int? = null)