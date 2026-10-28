package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.example.shared.domain.CardCount
import org.example.shared.domain.GameConfig
import org.example.project.ui.screens.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    viewModel: SettingsViewModel = koinViewModel(),
    onReset: () -> Unit = {}
) {
    val configState by viewModel.configState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface),
    ) {
        SettingsButtons(isLoading, viewModel, onReset)
        if (isLoading && configState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Ładowanie konfiguracji...")
                }
            }
        }
        if (configState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Błąd",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Nie udało się załadować konfiguracji",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(onClick = { viewModel.loadConfigFromServer() }) {
                        Text("Spróbuj ponownie")
                    }
                }
            }
        }

        SettingsContent(
            configState = configState,
            viewModel = viewModel,
            isLoading = isLoading
        )
    }
}

@Composable
private fun SettingsButtons(
    isLoading: Boolean,
    viewModel: SettingsViewModel,
    onReset: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextButton(
            onClick = { viewModel.loadConfigFromServer() },
            enabled = !isLoading,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Pobierz")
        }


        TextButton(
            onClick = {
                viewModel.reset()
                onReset()
            },
            enabled = !isLoading,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Resetuj")
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SaveButton(
    viewModel: SettingsViewModel,
    isLoading: Boolean
) {
    Row {
        Button(
            onClick = { viewModel.sendConfigToServer() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.surface
                )
            } else {
                Text("Zapisz i wyślij na serwer")
            }
        }
    }
}

@Composable
private fun SettingsContent(
    configState: GameConfig,
    viewModel: SettingsViewModel,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            PlayerCountSelector(
                currentCount = configState.maxPlayers,
                onCountChange = { viewModel.setMaxPlayers(it) }
            )
        }

        item {
            Text(
                "Konfiguracja Talii",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(configState.deckComposition) { cardCount ->
            CardConfigItem(
                cardCount = cardCount,
                onIncrement = { viewModel.updateCardCount(cardCount.cardNumber, 1) },
                onDecrement = { viewModel.updateCardCount(cardCount.cardNumber, -1) }
            )
        }
        item {
            SaveButton(viewModel, isLoading)
        }
    }
}

@Composable
private fun PlayerCountSelector(currentCount: Int, onCountChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            "Liczba graczy: ",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onCountChange(currentCount - 1) }) {
                    Icon(minus, contentDescription = "Odejmij kartę")
                }
                Text(
                    text = "$currentCount",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { onCountChange(currentCount + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj kartę")
                }
            }
        }
    }
}

@Composable
private fun CardConfigItem(
    cardCount: CardCount,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            "${cardCount.cardName} (${cardCount.cardNumber})",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(end = 8.dp)
        )

        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDecrement) {
                    Icon(
                        minus,
                        contentDescription = "Odejmij kartę",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = "${cardCount.count}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
                IconButton(onClick = onIncrement) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Dodaj kartę",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}


private val minus = materialIcon(name = "Minus") {
    materialPath {
        moveTo(5.0f, 11.0f)
        horizontalLineToRelative(14.0f)
        verticalLineToRelative(2.0f)
        horizontalLineTo(5.0f)
        close()
    }
}
