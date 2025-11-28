package org.example.project.UI.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ControlPanel(
    connectionStatus: String,
    onReset: () -> Unit
) {
    Column {
        Text(connectionStatus)

        Button(onClick = onReset) {
            Text("Reset")
        }
    }
}

