package org.example.project.UI.screens.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


@Composable
fun PriestRevealDialog(
    isRevealed: Boolean,
    targetPlayerName: String = "",
    revealedCardName: String? = null,
    onDismiss: () -> Unit
) {

    if (isRevealed && revealedCardName != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Wynik zagrania Kapłana") },
            text = {
                Text("Gracz $targetPlayerName ma na ręce kartę: $revealedCardName.")
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}