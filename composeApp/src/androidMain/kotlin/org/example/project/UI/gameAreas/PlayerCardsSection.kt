package org.example.project.UI.gameAreas


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.models.Card

@Composable
fun PlayerCardsSection(
    hand: List<Card>,
    selectedCard: Card?,
    onCardSelected: (Card) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hand.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    hand.forEach { card ->
                        val secondCard = hand.firstOrNull { it.id != card.id }
                        ClickableCard(
                            card = card,
                            isSelected = selectedCard?.id == card.id,
                            onCardClicked = {
                                onCardSelected(card)
                            },
                            secondCard = secondCard,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Text(
                    "Brak kart",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

