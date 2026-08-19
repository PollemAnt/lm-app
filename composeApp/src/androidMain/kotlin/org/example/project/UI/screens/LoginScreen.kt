package org.example.project.UI.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.example.project.UI.screens.viewModel.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: GameUiState,
    onJoin: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val isButtonEnabled = name.isNotBlank() && name.length >= 2 && !state.isLoading


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceTint
                    )
                )
            )
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary, thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Love Letter",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Dołącz do pokoju gry",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                state.gameConfig?.let { config ->
                    val currentPlayers = state.gameState?.players?.size ?: 0
                    Text(
                        text = "Gracze w lobby: $currentPlayers / ${config.maxPlayers}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (currentPlayers >= config.maxPlayers)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 15) name = it },
                label = { Text("Imię gracza") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                ),
                isError = name.isNotBlank() && name.length < 2,
                supportingText = {
                    if (name.isNotBlank() && name.length < 2) {
                        Text(
                            "Wymagane co najmniej 2 znaki",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = { onJoin(name) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isButtonEnabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Rozpocznij grę",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                text = "Po dołączeniu zostaniesz przekierowany do pokoju gry",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }
    }
}


