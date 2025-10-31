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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.models.Card
import org.example.models.Player

@Composable
fun PlayerGameArea(
    player: Player,
    hand: List<Card>?,
    players: List<Player>?,
    onPlayCard: (card: Card, opponent: Player?) -> Unit
) {
    var selectedCard by remember { mutableStateOf<Card?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (players != null) {
            OpponentsArea(
                player = player,
                players = players,
                selectedCard = selectedCard,
                onCardPlayed = { card, opponent ->
                    onPlayCard(card, opponent)
                    selectedCard = null
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PlayerBoardArea(
            player = player,
            selectedCard = selectedCard,
            onCardPlayed = { card ->
                onPlayCard(card, null)
                selectedCard = null
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ręka gracza
        Text("Twoje karty:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hand?.forEach { card ->
                ClickableCard(
                    card = card,
                    isSelected = selectedCard?.id == card.id,
                    onCardClicked = {
                        selectedCard = if (selectedCard?.id == card.id) null else card
                    }
                )
            }
        }
    }
}

@Composable
fun PlayerBoardArea(
    player: Player,
    selectedCard: Card?,
    onCardPlayed: (card: Card) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = if (selectedCard != null) 3.dp else 1.dp,
                color = if (selectedCard != null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = selectedCard != null,
                onClick = {
                    selectedCard?.let { card ->
                        onCardPlayed(card)
                    }
                }
            )
            .padding(8.dp)
    ) {
        Text("Twoja plansza", style = MaterialTheme.typography.titleMedium)

        if (player.playedCards.isNotEmpty()) {
            Text("Zagrane karty:", style = MaterialTheme.typography.bodySmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                player.playedCards.forEach { card ->
                    CardView(card)
                }
            }
        } else {
            Text("Brak zagranych kart", style = MaterialTheme.typography.bodySmall)
        }

        if (selectedCard != null) {
            Text(
                "Kliknij aby zagrać ${selectedCard.name} na planszę",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
