package org.example.project.UI.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun NumberPickerDialog(
    onNumberSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {

    val availableNumbers = listOf(
        0 to "Szpieg",
        2 to "Kapłan",
        3 to "Baron",
        4 to "Służąca",
        5 to "Książę",
        6 to "Kanclerz",
        7 to "Król",
        8 to "Hrabina",
        9 to "Księżniczka"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zgadnij kartę") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Wybierz numer karty:", style = MaterialTheme.typography.bodyMedium)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableNumbers.size) { index ->
                        val (number, name) = availableNumbers[index]
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onNumberSelected(number) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
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