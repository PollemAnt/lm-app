package org.example.project.UI

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.models.Player

@Composable
fun OpponentsArea(currentPlayer: Player, players: List<Player>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Gracze:", style = MaterialTheme.typography.titleLarge)
        Row {
            players.filter { it.id != currentPlayer.id }.forEach { opponent ->
                HiddenCardView(opponent.name, Modifier.weight(1f))
            }
        }
    }
}
