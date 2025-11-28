import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import org.example.models.Card
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CardView(card: Card) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${card.number}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                card.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun ClickableCard(
    card: Card,
    isSelected: Boolean,
    onCardClicked: () -> Unit,
    secondCard: Card?
) {
    val canBePlayed = canPlayCard(card, secondCard)
    val backgroundColor = when {
        !canBePlayed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                2.dp,
                when {
                    !canBePlayed -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                RoundedCornerShape(16.dp)
            )
            .clickable(
                enabled = canBePlayed,
                onClick = onCardClicked
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.Start) {
                Text("${card.number}", style = MaterialTheme.typography.titleLarge)
                Text(card.name, style = MaterialTheme.typography.titleMedium)
            }
            if (!canBePlayed) {
                Text(
                    "Nie można zagrać",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun canPlayCard(card: Card, secondCard: Card?): Boolean {
    if (card.number == 8)
        return checkCountessRequirement(secondCard)
    return card.number != 9
}

fun checkCountessRequirement(secondCard: Card?): Boolean {
    return secondCard?.number != 5 && secondCard?.number != 7
}

fun canPlayOnOwnBoard(card: Card, allOpponentsProtected: Boolean?): Boolean {
    return when (card.number) {
        0, 4, 5, 6, 8 -> true
        else -> allOpponentsProtected == true
    }
}

fun canPlayOnOpponent(card: Card): Boolean {
    return when (card.number) {
        1, 2, 3, 7, 5 -> true
        else -> false
    }
}