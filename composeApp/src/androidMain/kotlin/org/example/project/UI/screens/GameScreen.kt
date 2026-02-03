package org.example.project.UI.screens

import OpponentsArea
import PlayerBoardView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import org.example.project.UI.gameAreas.GameMessageBar
import org.example.project.UI.gameAreas.PlayerCardsSection
import org.example.project.UI.screens.dialogs.ChancellorCardSelectionDialog
import org.example.project.UI.screens.dialogs.NumberPickerDialog
import org.example.project.UI.screens.dialogs.PriestRevealDialog
import org.example.project.UI.screens.dialogs.RoundSummaryDialog
import org.example.project.UI.screens.viewModel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val chancellorState by viewModel.chancellorState.collectAsState()
    val priestRevealState by viewModel.priestRevealState.collectAsState()
    val roundSummaryState by viewModel.roundSummaryState.collectAsState()
    var showNumberPickerFor by remember { mutableStateOf<Pair<Card, Player>?>(null) }

    var selectedCard by remember { mutableStateOf<Card?>(null) }

    val displayState = state


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.primary,
            )
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary, thickness = 2.dp)

        if (displayState.message != null) {
            GameMessageBar(
                message = displayState.message,
                remainingCards = displayState.gameState?.cards?.size ?: 0
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary, thickness = 2.dp)

        displayState.gameState?.let { gameState ->
            val player = displayState.player!!
            val opponents = gameState.players.filter { it.id != player.id }
            val allOpponentsProtected = opponents.all { it.isProtected }


            if (opponents.isNotEmpty() == true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OpponentsArea(
                        player = player,
                        players = gameState.players,
                        activePlayerId = gameState.activePlayerId ?: 0,
                        selectedCard = selectedCard,
                        onCardPlayed = { card, opponent ->
                            if (card.number == 1 && opponent != null && opponent.id != player.id) {
                                showNumberPickerFor = card to opponent
                            } else {
                                viewModel.makeMove(card, opponent, null)
                            }
                            selectedCard = null
                        }
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary, thickness = 2.dp)

            PlayerBoardView(
                player = player,
                selectedCard = selectedCard,
                activePlayerId = gameState.activePlayerId ?: 0,
                onCardPlayed = { card ->
                    viewModel.makeMove(card, player, null)
                    selectedCard = null
                },
                allOpponentsProtected = allOpponentsProtected
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondary, thickness = 2.dp)

            PlayerCardsSection(
                hand = displayState.hand,
                selectedCard = selectedCard,
                onCardSelected = { card ->
                    selectedCard = if (selectedCard?.id == card.id) null else card
                }
            )

        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Ładowanie stanu gry...")
                }
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
        ChancellorCardSelectionDialog(
            availableCards = it.availableCards,
            onCardSelected = { selectedCard ->
                viewModel.completeChancellorMove(selectedCard)
            },
            onDismiss = { }
        )
    }

    priestRevealState?.let {
        PriestRevealDialog(
            isRevealed = it.isRevealed,
            targetPlayerName = it.targetPlayerName,
            revealedCard = it.revealedCard,
            onDismiss = { viewModel.dismissPriestReveal() }
        )
    }

    roundSummaryState?.let {
        RoundSummaryDialog(
            roundSummary = it,
            onDismiss = { viewModel.clearRoundSummary() }
        )
    }
}