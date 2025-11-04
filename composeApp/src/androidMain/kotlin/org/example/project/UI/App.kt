package org.example.project.UI

import PlayerGameArea
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.project.viewModel.GameViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.models.Card
import org.example.project.UI.gameAreas.dialogs.ChancellorCardSelectionDialog
import org.example.project.UI.gameAreas.dialogs.NumberPickerDialog
import org.example.project.viewModel.GameUiState

@Composable
fun App(viewModel: GameViewModel = viewModel()) {

    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()

    viewModel.loadInitialState()
    GameScreen(viewModel, state, coroutineScope)
}

@Composable
private fun GameScreen(
    viewModel: GameViewModel,
    state: GameUiState,
    coroutineScope: CoroutineScope
) {
    var selectedNumber by remember { mutableStateOf<Int?>(null) }
    var showNumberPicker by remember { mutableStateOf(false) }
    var pendingCard by remember { mutableStateOf<Card?>(null) }
    var pendingOpponent by remember { mutableStateOf<Player?>(null) }

    val chancellorState by viewModel.chancellorState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ControlPanel(state.isConnected, viewModel)

        if (state.player == null) {
            LoginScreen(coroutineScope, viewModel)
        } else if (state.gameState != null) {

            PlayerInformation(state.player, state.gameState)

            Spacer(Modifier.height(16.dp))

            PlayerGameArea(
                player = state.player,
                hand = state.hand,
                players = state.gameState.players,
                onPlayCard =
                    { card, opponent ->
                        if (card.number == 1 && opponent != null) {
                            pendingCard = card
                            pendingOpponent = opponent
                            showNumberPicker = true
                            selectedNumber = null
                        } else {
                            viewModel.makeMove(card, opponent, -1)
                        }
                    }
            )

            if (showNumberPicker) {
                NumberPickerDialog(
                    onNumberSelected = { number ->
                        selectedNumber = number
                        pendingCard?.let { card ->
                            pendingOpponent?.let { opponent ->
                                viewModel.makeMove(card, opponent, number)
                            }
                        }
                        showNumberPicker = false
                        pendingCard = null
                        pendingOpponent = null
                    },
                    onDismiss = {
                        showNumberPicker = false
                        pendingCard = null
                        pendingOpponent = null
                    }
                )
            }
            if (chancellorState != null) {
                ChancellorCardSelectionDialog(
                    availableCards = chancellorState!!.availableCards,
                    onCardSelected = { selectedCard ->
                        viewModel.completeChancellorMove(selectedCard)
                    },
                    onDismiss = {
                        viewModel.completeChancellorMove(chancellorState!!.availableCards.first())
                    }
                )
            }

            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
private fun ControlPanel(
    isConnected: String?,
    viewModel: GameViewModel
) {
    Text(isConnected ?: "Brak polaczenia z serwerem")

    Button(onClick = {
        CoroutineScope(Dispatchers.Main).launch { viewModel.reset() }
    }) {
        Text("Reset")
    }
}

@Composable
private fun PlayerInformation(
    player: Player,
    gameState: GameSnapshot
) {
    Text("Jesteś: ${player.name} (id=${player.id})")

    Text(
        "Aktywny gracz: ${
            gameState.players.get(
                gameState.activePlayerId!!
            ).name
        } ID: ${gameState.activePlayerId} "
    )
}

@Composable
private fun LoginScreen(
    coroutineScope: CoroutineScope,
    viewModel: GameViewModel
) {
    var name by remember { mutableStateOf("") }

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Twoje imię: ") })
    Button(onClick = {
        coroutineScope.launch {
            viewModel.join(name)
        }
    }) {
        Text("Dołącz do gry")
    }
}