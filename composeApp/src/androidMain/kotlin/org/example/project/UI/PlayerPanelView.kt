import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import org.example.Card
import org.example.models.Player

@Composable
fun PlayerHand(
    playerId: Int,
    players: List<Player>?,
    onDropCard: (card: Card, opponent: Player) -> Unit
) {
    val player = players?.find{ it.id == playerId}

    println("XXX PlayerHand $player")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text("Twoje karty", style = MaterialTheme.typography.titleLarge)
        Row {
            player?.hand?.forEach { card ->
                println("DOdawanie card: $card")
                key(card.id) {
                    DraggableCard(
                        card,
                        opponents = players.filter { it.id != player.id } ?: emptyList(),
                        onDropOnOpponent = { opponent ->
                            onDropCard(card, opponent)
                        })
                }
            }
        }
    }
}
