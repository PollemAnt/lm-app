package org.example.project.ui.gameAreas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.example.shared.domain.CardType.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.shared.domain.Card

@Composable
fun SmallCardView(card: Card) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceTint
                    )
                )
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${card.number}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Text(
                card.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,

                )
        }
    }
}

@Composable
fun ClickableCard(
    card: Card,
    isSelected: Boolean,
    onCardClicked: () -> Unit,
    secondCard: Card?,
    modifier: Modifier = Modifier
) {
    val canBePlayed = canPlayCard(card, secondCard)

    Card(
        modifier = modifier
            .clickable(
                enabled = canBePlayed,
                onClick = onCardClicked
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = when {
            !canBePlayed -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
            isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary)
            else -> null
        },
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceBright
                        )
                    )
                )
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "${card.number}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    !canBePlayed -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                card.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = when {
                    !canBePlayed -> MaterialTheme.colorScheme.onErrorContainer
                    isSelected -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )

            if (!canBePlayed) {
                Text(
                    "Nie można zagrać",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (isSelected) {
                Text(
                    "✓ Wybrano",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

fun canPlayCard(card: Card, secondCard: Card?): Boolean {
    if (card.cardType == PRINCE || card.cardType == KING)
        return checkCountessRequirement(secondCard)
    return card.cardType != PRINCESS
}

fun checkCountessRequirement(secondCard: Card?): Boolean {
    return secondCard?.cardType != COUNTESS
}

fun canPlayOnOwnBoard(card: Card, allOpponentsProtected: Boolean?): Boolean {
    return when (card.cardType) {
        SPY, HANDMAID, PRINCE, CHANCELLOR, COUNTESS -> true
        else -> allOpponentsProtected == true
    }
}

fun canPlayOnOpponent(card: Card): Boolean {
    return when (card.cardType) {
        GUARD, PRIEST, BARON, KING, PRINCE -> true
        else -> false
    }
}
