package org.example.project.UI.gameAreas.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card as ComposeCard
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.websocket.Frame.Text
import org.example.models.Card

@Composable
fun ChancellorCardSelectionDialog(
    availableCards: List<Card>,
    onCardSelected: (Card) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz kartę do zachowania") },
        text = {
            Column {
                Text("Wybierz jedną kartę, którą chcesz zachować. Pozostałe zostaną odłożone na talię.")
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow {
                    items(availableCards.size) { index -> // card to TWOJA klasa z gry
                        ComposeCard( // Używam Surface zamiast Card żeby uniknąć konfliktu nazw
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { onCardSelected(availableCards[index]) },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Text(availableCards[index].name)
                            Text(text = "Nr: ${availableCards[index].number}")
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