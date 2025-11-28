package org.example.project.UI.screens.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.example.models.Card


@Composable
fun PriestRevealDialog(
    isRevealed: Boolean,
    targetPlayerName: String = "",
    revealedCard: Card? = null,
    onDismiss: () -> Unit
) {

    if (isRevealed && revealedCard != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Wynik zagrania Kapłana") },
            text = {
                Text("Gracz ${targetPlayerName} ma na ręce kartę: ${revealedCard.name}.")
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}