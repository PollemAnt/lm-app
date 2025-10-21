import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.Card
import org.example.models.Player

@Composable
fun PlayerGameArea(
    playerId: Int,
    hand: List<Card>?,
    players: List<Player>?,
    onDropCard: (card: Card, opponent: Player) -> Unit
) {
    val player = players?.find { it.id == playerId }

    println("XXX PlayerHand $player")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (players != null) {
            OpponentsArea(
                playerId = playerId,
                players = players
            ) { cardId, opponent ->
                println("cardId $cardId   opponent  $opponent")
                val card = hand?.find { it.id == cardId.toInt() }
                if (card != null) {
                    onDropCard(card, opponent)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Twoje karty", style = MaterialTheme.typography.titleLarge)
        Row {
            hand?.forEach { card ->
                println("Dodawanie card: $card")
                DraggableCard(card)
            }
        }
    }
}