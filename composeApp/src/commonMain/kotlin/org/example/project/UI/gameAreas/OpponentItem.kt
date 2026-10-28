package org.example.project.ui.gameAreas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.shared.domain.Card
import org.example.shared.domain.Player

@Composable
fun OpponentItem(
    opponent: Player,
    selectedCard: Card?,
    activePlayerId: Int,
    onCardPlayed: (card: Card) -> Unit
) {
    val canCardTargetOpponent = selectedCard?.let { canPlayOnOpponent(it) } == true
    val isOpponentProtected = opponent.isProtected
    val canPlayOnThisOpponent = canCardTargetOpponent && !isOpponentProtected
    val isSelectable = selectedCard != null && canPlayOnThisOpponent
    val isActive = opponent.id == activePlayerId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelectable) 8.dp else 4.dp,
            pressedElevation = 12.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isOpponentProtected)
                MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelectable) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.tertiary
        ) else if (selectedCard == null) {
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primaryContainer
            )
        } else BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.error
        ),
        onClick = {
            if (isSelectable) {
                onCardPlayed(selectedCard)
            }
        },
        enabled = isSelectable
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceBright
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = opponent.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Points",
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${opponent.points}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (isActive) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Active Player",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "TURA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                if (opponent.playedCards.isNotEmpty()) {
                    Column {
                        Text(
                            "Zagrane karty:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .horizontalScroll(rememberScrollState())
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            items(opponent.playedCards.size) { index ->
                                val card = opponent.playedCards[index]
                                SmallCardView(card = card)
                            }
                        }
                    }
                } else {
                    Text(
                        "Brak zagranych kart",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (isOpponentProtected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Gracz chroniony",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "Gracz chroniony",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
