package org.example.storage

import com.russhwolf.settings.Settings

class GameStorage(private val settings: Settings = Settings()) {

    fun savePlayerId(playerId: Int) {
        settings.putInt(KEY_PLAYER_ID, playerId)
    }

    fun getPlayerId(): Int? {
        val id = settings.getInt(KEY_PLAYER_ID, -1)
        return if (id != -1) id else null
    }

    fun clearPlayerId() {
        settings.remove(KEY_PLAYER_ID)
    }

    companion object {
        private const val KEY_PLAYER_ID = "active_player_id"
    }
}
