package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.project.UI.OpponentsArea
import org.example.project.UI.PlayerHand

@Composable
fun App() {

    val api = remember { ApiService("https://lm-serwer.onrender.com") }
    val coroutineScope = rememberCoroutineScope()

    var player by remember { mutableStateOf<Player?>(null) }
    var gameState by remember { mutableStateOf<GameSnapshot?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isConnected = api.ping()
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize()
            .padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(isConnected ?: "Brak polaczenia z serwerem")

        if (player == null) {
            var name by remember { mutableStateOf("") }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Twoje imię: ") })
            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    val p = api.join(name)
                    player = p
                    gameState = api.getState()
                }
            }) {
                Text("Dołącz do gry")
            }
        } else {
            // ekran gry
            Text("Jesteś: ${player!!.name} (id=${player!!.id})")

            LaunchedEffect(Unit) {
                api.connectToUpdates { event ->
                    when (event.type) {
                        "player_joined" -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                gameState = api.getState()
                            }
                           // message = "Gracz ${event.player?.name} dołączył do gry"

                        }

                        "draw" -> {
                           println("DRAW EVENT")
                        }

                        "move" -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                gameState = api.getState()
                            }
                        }
                    }
                    //message = "brak eventu"
                }
            }


            Text(
                "Aktywny gracz: ${gameState?.activePlayerId} ${
                    gameState?.players?.get(
                        gameState?.activePlayerId!!
                    )
                }"
            )

            OpponentsArea(
                currentPlayer = player!!,
                players = gameState?.players ?: emptyList()
            )

            Spacer(Modifier.height(16.dp))

            PlayerHand(player!!)

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    val res = api.makeMove(player!!.id, card = player!!.hand[0], targetPlayerId = 1)
                    message = res.ok ?: res.error
                    gameState = api.getState()
                }
            }) {
                Text("Wykonaj ruch")
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    val card = api.drawCard(player!!.id)
                    message = "Dobrałeś kartę: ${card.name}"
                    val hand = api.getHand(player!!.id)
                    player = player!!.copy(hand = hand)
                }
            }) {
                Text("Dobierz kartę")
            }

            Spacer(Modifier.height(16.dp))

            // Wyświetlanie kart w ręce
            Text("Twoje karty:")
            player?.hand?.forEach { card ->
                Text(" - ${card.name} (${card.number}): ${card.description}")
            }

            //message?.let { Text(it) }

            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch { api.getReset() }
            }) {
                Text("Reset")
            }
        }
    }
}

