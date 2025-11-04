import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.models.Card
import org.example.models.Player
import kotlin.random.Random

@Composable
fun OpponentsArea(
    player: Player,
    players: List<Player>,
    selectedCard: Card?,
    onCardPlayed: (card: Card, opponent: Player) -> Unit
) {
    val colors = remember {
        (0..players.size + 1).map { Color(Random.nextLong()).copy(alpha = 1f) }
    }

    val canPlayOnOpponent = selectedCard?.let { canPlayOnOpponent(it) } ?: false

    Text("Gracze:", style = MaterialTheme.typography.titleLarge)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        players.filter { it.id != player.id }.forEachIndexed { index, opponent ->
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .background(colors[index])
                    .clickable(
                        enabled = selectedCard != null && canPlayOnOpponent,
                        onClick = {
                            selectedCard?.let { card ->
                                onCardPlayed(card, opponent)
                            }
                        }
                    )
                    .border(
                        width = if (selectedCard != null && canPlayOnOpponent) 3.dp else 0.dp,
                        color = if (selectedCard != null && canPlayOnOpponent) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (selectedCard != null) {
                        if (canPlayOnOpponent) {
                            Text(
                                "Kliknij aby zagrać ${selectedCard.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                "Nie można zagrać ${selectedCard.name} na przeciwnika",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Text("Gracz ${opponent.name}", style = MaterialTheme.typography.bodySmall)
                    Text("Zagrane karty:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        opponent.playedCards.forEach { card ->
                            CardView(card)
                        }
                    }


                }
            }
        }
    }
}


