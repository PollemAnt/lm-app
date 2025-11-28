package org.example.project.UI.screens

import PlayerGameArea
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.models.Card
import org.example.models.Player
import org.example.project.UI.components.ControlPanel
import org.example.project.UI.components.PlayerInformation
import org.example.project.UI.components.dialogs.ChancellorCardSelectionDialog
import org.example.project.UI.components.dialogs.NumberPickerDialog
import org.example.project.UI.screens.viewModel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val chancellorState by viewModel.chancellorState.collectAsState()
    var showNumberPickerFor by remember { mutableStateOf<Pair<Card, Player>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        state.gameState?.let { gameState ->
            ControlPanel(
                connectionStatus = state.isConnected ?: "Brak połączenia z serwerem",
                onReset = { viewModel.reset() }
            )

            PlayerInformation(
                player = state.player!!,
                gameState = gameState
            )

            Spacer(Modifier.height(16.dp))

            PlayerGameArea(
                player = state.player!!,
                hand = state.hand,
                players = gameState.players,
                onPlayCard = { card, opponent ->
                    if (card.number == 1 && opponent != null) {
                        showNumberPickerFor = card to opponent
                    } else {
                        viewModel.makeMove(card, opponent, null)
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            state.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
    }


    showNumberPickerFor?.let { (card, opponent) ->
        NumberPickerDialog(
            onNumberSelected = { guessingCardNumber ->
                viewModel.makeMove(card, opponent, guessingCardNumber)
                showNumberPickerFor = null
            },
            onDismiss = { showNumberPickerFor = null }
        )
    }

    chancellorState?.let {
        println("[DEBUG] GameScreen chancellorState: $chancellorState")
        ChancellorCardSelectionDialog(
            availableCards = it.availableCards,
            onCardSelected = { selectedCard -> viewModel.completeChancellorMove(selectedCard) },
            onDismiss = { }
        )
    }
}