package org.example.project.UI.screens

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.sp
import org.example.models.CardCount
import org.example.models.GameConfig
import org.example.project.UI.screens.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    viewModel: SettingsViewModel = viewModel()
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
 Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia Gry") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.loadConfigFromServer() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Odśwież konfigurację"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { viewModel.sendConfigToServer() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                enabled = configState != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Zapisz i wyślij na serwer")
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading && configState == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
            configState == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
            else -> {
                SettingsContent(
                    configState = configState,
                    viewModel = viewModel,
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    configState: GameConfig,
    viewModel: SettingsViewModel,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
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
            Spacer(modifier = Modifier.height(8.dp))
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
    }
}

@Composable
private fun PlayerCountSelector(currentCount: Int, onCountChange: (Int) -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Maksymalna liczba graczy", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                IconButton(onClick = { onCountChange(currentCount - 1) }) {
                    Icon(minus, contentDescription = "Zmniejsz liczbę graczy")
                }
                Text(
                    text = "$currentCount",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { onCountChange(currentCount + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Zwiększ liczbę graczy")
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${cardCount.cardName} (${cardCount.cardNumber})",
                style = MaterialTheme.typography.bodyLarge
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement) {
                    Icon(minus, contentDescription = "Odejmij kartę")
                }
                Text(
                    text = "${cardCount.count}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrement) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj kartę")
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