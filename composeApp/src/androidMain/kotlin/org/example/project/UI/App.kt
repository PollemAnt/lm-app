package org.example.project.UI

import PlayerGameArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.project.viewModel.GameViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.models.Card
import org.example.project.viewModel.GameUiState

@Composable
fun App(viewModel: GameViewModel = viewModel()) {

    val coroutineScope = rememberCoroutineScope()
    val state by viewModel.uiState.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.connectToUpdates()
    }
    viewModel.loadInitialState()
    GameScreen(viewModel, state, coroutineScope)
}

@Composable
private fun GameScreen(
    viewModel: GameViewModel,
    state: GameUiState,
    coroutineScope: CoroutineScope
) {
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

            PlayerInformation(state.hand, state.player, state.gameState)

            Spacer(Modifier.height(16.dp))

            PlayerGameArea(
                player = state.player,
                hand = state.hand,
                players = state.gameState.players,
                onPlayCard =
                    { card, opponent ->
                        viewModel.makeMove(card, opponent)
                    }
            )

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
    hand: List<Card>,
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

    /*repeat(hand.size) {
        Text("${hand.size} Karta: ${hand[it].name}")
    }

    val played = gameState.players.get(player.id).playedCards
    repeat(played.size) {
        Text("zagrana: ${played[it].name}")
    }*/
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

@Composable
private fun ButtonsPanel(
    coroutineScope: CoroutineScope,
    player: Player,
    gameState: GameSnapshot,
    viewModel: GameViewModel
) {
    Row {
        Button(onClick = {
            viewModel.drawCard()
        }) {
            Text("Dobierz kartę")
        }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            viewModel.loadInitialState()
        }) {
            Text("aktual")
        }
    }
}
