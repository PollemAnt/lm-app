package org.example.project.UI.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card as ComposeCard
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.shared.domain.Card

@Composable
fun ChancellorCardSelectionDialog(
    availableCards: List<Card>,
    onCardSelected: (Card) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Wybierz kartę do zachowania",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text("Wybierz jedną kartę, którą chcesz zachować. Pozostałe zostaną odłożone na talię.")
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow {
                    items(availableCards.size) { index ->
                        ComposeCard(
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { onCardSelected(availableCards[index]) },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = availableCards[index].name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Nr: ${availableCards[index].number}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
