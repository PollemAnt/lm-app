package org.example.project.UI.screens

import org.example.project.UI.gameAreas.OpponentsArea
import org.example.project.UI.gameAreas.PlayerBoardView
import org.example.project.UI.gameAreas.PlayerCardsSection
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.shared.domain.Card
import org.example.shared.domain.Player
import org.example.project.UI.gameAreas.GameMessageBar
import org.example.project.UI.gameAreas.canPlayOnOpponent
import org.example.project.UI.screens.dialogs.ChancellorCardSelectionDialog
import org.example.project.UI.screens.dialogs.NumberPickerDialog
import org.example.project.UI.screens.dialogs.PriestRevealDialog
import org.example.project.UI.screens.dialogs.RoundSummaryDialog
import org.example.project.UI.screens.viewModel.GameUiEvent
import org.example.project.UI.screens.viewModel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel
) {
    val screenId = remember { (1000..9999).random().toString() }
    println("DEBUG - GameScreen instance $screenId")
    val state by viewModel.uiState.collectAsState()

    var selectedCard by remember { mutableStateOf<Card?>(null) }
    var showNumberPickerFor by remember { mutableStateOf<Pair<Card, Player>?>(null) }

    var priestDialog by remember { mutableStateOf<GameUiEvent.ShowPriestReveal?>(null) }
    var roundSummaryDialog by remember { mutableStateOf<GameUiEvent.ShowRoundSummary?>(null) }
    var messages by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(viewModel) {
        println("DEBUG - collector started in screen $screenId")
        viewModel.uiEvents.collect { event ->
            println("DEBUG - GameScreen collect event $event")
            when (event) {

                is GameUiEvent.ShowMessage -> {
                    messages = (messages + event.message).takeLast(20)
                }

                is GameUiEvent.ShowPriestReveal -> {
                    println("DEBUG - GameScreen ShowPriestReveal")
                    priestDialog = event
                }

                is GameUiEvent.ShowRoundSummary -> {
                    roundSummaryDialog = event
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSecondary,
            thickness = 2.dp
        )

        GameMessageBar(
            messages = messages,
            remainingCards = state.gameState?.cards?.size ?: 0
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSecondary,
            thickness = 2.dp
        )

        state.gameState?.let { gameState ->
            state.player?.let { player ->

                val allOpponentsProtected by remember(gameState, player.id) {
                    derivedStateOf {
                        gameState.players
                            .filter { it.id != player.id }
                            .let { opponents ->
                                opponents.isNotEmpty() && opponents.all { it.isProtected }
                            }
                    }
                }

                val activePlayerId = gameState.activePlayerId

                val opponents = gameState.players.filter { it.id != player.id }

                if (opponents.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OpponentsArea(
                            player = player,
                            players = gameState.players,
                            activePlayerId = activePlayerId,
                            selectedCard = selectedCard,
                            onCardPlayed = { card, opponent ->
                                if (card.number == 1 &&
                                    opponent != null &&
                                    opponent.id != player.id
                                ) {
                                    showNumberPickerFor = card to opponent
                                } else {
                                    viewModel.makeMove(card, opponent, null)
                                }
                                selectedCard = null
                            }
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSecondary,
                    thickness = 2.dp
                )

                PlayerBoardView(
                    player = player,
                    selectedCard = selectedCard,
                    activePlayerId = activePlayerId,
                    onCardPlayed = { card ->
                        viewModel.makeMove(card, player, null)
                        selectedCard = null
                    },
                    allOpponentsProtected = allOpponentsProtected
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSecondary,
                    thickness = 2.dp
                )

                PlayerCardsSection(
                    hand = state.hand,
                    selectedCard = selectedCard,
                    onCardSelected = { card ->
                        selectedCard =
                            if (selectedCard?.id == card.id) null else card
                    }
                )

                selectedCard?.let { card ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (canPlayOnOpponent(card))
                                "Wybierz gracza, aby użyć ${card.name}"
                            else "Kliknij na swoją planszę, aby zagrać ${card.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
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
            onNumberSelected = { guessedNumber ->
                viewModel.makeMove(card, opponent, guessedNumber)
                showNumberPickerFor = null
            },
            onDismiss = { showNumberPickerFor = null }
        )
    }

    state.chancellorState?.let { chancellorState ->
        ChancellorCardSelectionDialog(
            availableCards = chancellorState.availableCards,
            onCardSelected = { chosenCard ->
                viewModel.completeChancellorMove(chosenCard)
            },
            onDismiss = { }
        )
    }

    priestDialog?.let { event ->
        println("DEBUG - GameScreen Priest dialog")
        PriestRevealDialog(
            isRevealed = true,
            targetPlayerName = event.targetPlayerName,
            revealedCardName = event.revealedCardName,
            onDismiss = { priestDialog = null }
        )
    }

    roundSummaryDialog?.let { event ->
        RoundSummaryDialog(
            roundSummary = event.summary,
            onDismiss = { roundSummaryDialog = null }
        )
    }
}
