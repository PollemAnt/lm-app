import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.models.Card
import org.example.models.Player

@Composable
fun OpponentsArea(
    player: Player,
    players: List<Player>,
    selectedCard: Card?,
    onCardPlayed: (card: Card, opponent: Player) -> Unit
) {
    val opponents = players.filter { it.id != player.id }
    val canCardTargetOpponent = selectedCard?.let { canPlayOnOpponent(it) } ?: false

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opponents.forEach { opponent ->

            val isOpponentProtected = opponent.isProtected
            val canPlayOnThisOpponent = canCardTargetOpponent && !isOpponentProtected

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .border(
                        width = if (canPlayOnThisOpponent) 3.dp else 1.dp,
                        color = if (canPlayOnThisOpponent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        enabled = canPlayOnThisOpponent,
                        onClick = {
                            selectedCard?.let { card ->
                                onCardPlayed(card, opponent)
                            }
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    if (selectedCard != null) {
                        val message: String
                        val messageColor: Color
                        when {
                            isOpponentProtected -> {
                                message = "Gracz jest chroniony"
                                messageColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                            canCardTargetOpponent -> {
                                message = "Kliknij, aby zagrać ${selectedCard.name}"
                                messageColor = MaterialTheme.colorScheme.primary
                            }
                            else -> {
                                message = "Nie można zagrać ${selectedCard.name} na gracza"
                                messageColor = MaterialTheme.colorScheme.error
                            }
                        }
                        Text(message, style = MaterialTheme.typography.bodyMedium, color = messageColor)
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Text("Gracz ${opponent.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Zagrane karty:", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        opponent.playedCards.forEach { card ->
                            CardView(card)
                        }
                    }
                }
                if (isOpponentProtected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Blue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Gracz chroniony",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}


