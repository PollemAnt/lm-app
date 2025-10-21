package org.example.project.UI

import OpponentsArea
import PlayerHand
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
import org.example.ApiService
import org.example.project.viewModel.GameViewModel
import org.example.project.providePlatformScope

@Composable
fun App() {
    val viewModel = remember {
        GameViewModel(
            ApiService("https://lm-serwer.onrender.com"),
            providePlatformScope()
        )
    }

    val player by viewModel.player.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val message by viewModel.message.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.connect()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(isConnected ?: "Brak polaczenia z serwerem")

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch { viewModel.reset() }
        }) {
            Text("Reset")
        }

        if (player == null) {
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
        } else if (gameState != null) {

            Text("Jesteś: ${player!!.name} (id=${player!!.id})")

            Text(
                "Aktywny gracz: ${gameState?.activePlayerId} ${
                    gameState?.players?.get(
                        gameState?.activePlayerId!!
                    )
                }"
            )

            OpponentsArea(currentPlayer = player!!, players = gameState?.players ?: emptyList())

            Spacer(Modifier.height(16.dp))

            PlayerHand(
                player!!.id, gameState?.players,
                onDropCard =
                    { card, opponent ->
                        viewModel.makeMove(card, opponent)
                    }
            )

            Spacer(Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    coroutineScope.launch {
                        player!!.hand.firstOrNull()?.let { card ->
                            val opponent = gameState?.players?.firstOrNull { it.id != player!!.id }
                            if (opponent != null) viewModel.makeMove(card, opponent)
                        }
                    }
                }) {
                    Text("Wykonaj ruch")
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    viewModel.drawCard()
                }) {
                    Text("Dobierz kartę")
                }
            }

            Spacer(Modifier.height(16.dp))

            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}