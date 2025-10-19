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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.ApiService
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.project.UI.OpponentsArea
import org.example.project.UI.PlayerPanelView
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    /*
    MaterialTheme {
        val scope = rememberCoroutineScope()
        val api = remember { ApiService("http://192.168.1.24:8080") }
        var state by remember { mutableStateOf<GameSnapshot?>(null) }
        var result by remember { mutableStateOf<Map<String,Any>?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                scope.launch {
                    try {
                        state = api.getState()
                        error = null
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }) {
                Text("Pobierz stan gry")
            }
            Button(onClick = {
                scope.launch {
                    try {
                        result = api.makeMove(playerId = 1 )
                        error = null
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }) {
                Text("Zrób ruch")
            }

            Spacer(Modifier.height(16.dp))

            error?.let {
                Text("Błąd: $it", color = MaterialTheme.colorScheme.error)
            }

            state?.let {
                Text("Aktywny gracz ID: ${it.activePlayerId}")
                Spacer(Modifier.height(8.dp))
                it.players.forEach { p ->
                    Text("Gracz ${p.id}: ${p.name}")
                }
            }
            result?.let{
                Text("Move result keys: ${it.keys}")
                Spacer(Modifier.height(8.dp))
                Text("Move result values: ${it.values}")
            }
        }
    }*/
    val api = remember { ApiService("https://lm-serwer.onrender.com") }
    var player by remember { mutableStateOf<Player?>(null) }
    var gameState by remember { mutableStateOf<GameSnapshot?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    var isConnected by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isConnected = api.ping()
    }

    /*LaunchedEffect(player) {
        if (player != null) {
            while (true) {
                try {
                    val snapshot = api.getState()
                    gameState = snapshot
                    val updatedPlayer = api.getPlayer(player!!.id)
                    player = updatedPlayer
                } catch (e: Exception) {
                    println("Błąd synchronizacji: $e")
                }
                delay(2000) // 2 sekundy
            }
        }
    }*/

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize()
            .padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(isConnected ?: "nnnnnnnna")

        if (player == null) {
            var name by remember { mutableStateOf("") }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Twoje imię") })
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
                        }

                        "card_draw" -> {
                            if (event.player?.id == player?.id) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    player = api.getPlayer(player!!.id)
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    gameState = api.getState()
                                }

                            }
                        }

                        "turn_changed" -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                gameState = api.getState()
                            }
                        }
                    }
                }
            }


            Text(
                "Aktywny gracz: ${gameState?.activePlayerId} ${
                    gameState?.players?.get(
                        gameState?.activePlayerId!!
                    )
                }"
            )

            OpponentsArea().OpponentsArea(
                currentPlayer = player!!,
                players = gameState?.players ?: emptyList()
            )

            Spacer(Modifier.height(16.dp))

            PlayerPanelView().PlayerHand(player!!)

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    val res = api.makeMove(player!!.id)
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

            message?.let { Text(it) }
        }
    }
}

