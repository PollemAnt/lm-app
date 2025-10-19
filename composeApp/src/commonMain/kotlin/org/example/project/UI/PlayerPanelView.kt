package org.example.project.UI

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.models.Player

class PlayerPanelView {

    @Composable
    fun PlayerHand(player: Player){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text("Twoje karty", style = MaterialTheme.typography.titleLarge)
            Row{
                player.hand.forEach{card ->
                    CardsView().CardView(card, Modifier.weight(1f))
                }
            }
        }
    }
}