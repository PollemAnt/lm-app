package org.example.project.UI.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.example.models.GameSnapshot
import org.example.models.Player

@Composable
fun PlayerInformation(
    player: Player,
    gameState: GameSnapshot
) {

    Text("Jesteś: ${player.name} (id=${player.id})")

    val activePlayer = gameState.activePlayerId?.let { gameState.players.getOrNull(it) }
    if (activePlayer != null) {
        Text("Aktywny gracz: ${activePlayer.name} ID: ${activePlayer.id}")
    } else {
        Text("Oczekiwanie na aktywnego gracza...")
    }
}
