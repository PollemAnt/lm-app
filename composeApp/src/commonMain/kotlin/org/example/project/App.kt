package org.example.project
/*
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
import org.example.models.CardPlayedEvent
import org.example.models.GameSnapshot
import org.example.models.Player
import org.example.models.PlayerJoinEvent
import org.example.models.TurnChangedEvent

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

    LaunchedEffect(Unit) {
        api.connectToUpdates { event ->
            when (event) {

                is CardPlayedEvent -> coroutineScope.launch {
                    gameState = api.getState()
                    println("DRAW CARD")
                }

                is PlayerJoinEvent -> coroutineScope.launch {
                    println("PLAYER JOINED")
                    gameState = api.getState()
                }

                is TurnChangedEvent -> coroutineScope.launch {
                    gameState = api.getState()
                }
            }
        }
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
            CoroutineScope(Dispatchers.Main).launch { api.getReset() }
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
                    player = api.join(name)
                    gameState = api.getState()
                }
            }) {
                Text("Dołącz do gry")
            }
        } else {
            // ekran gry
            Text("Jesteś: ${player!!.name} (id=${player!!.id})")


            Text(
                "Aktywny gracz: ${gameState?.activePlayerId} ${
                    gameState?.players?.get(
                        gameState?.activePlayerId!!
                    )
                }"
            )

            //OpponentsArea(currentPlayer = player!!, players = gameState?.players ?: emptyList())

            Spacer(Modifier.height(16.dp))

           /* PlayerHand(
                player!!,
                opponents = gameState?.players?.filter { it.id != player!!.id } ?: emptyList(),
                onDropCard =
                    { card, opponent ->
                        coroutineScope.launch {
                            val res = api.makeMove(player!!.id, card, opponent.id)
                            message = res.ok
                            gameState = api.getState()
                        }
                    })*/

            Spacer(Modifier.height(16.dp))

            Row {
                Button(onClick = {
                    coroutineScope.launch {
                        val res =
                            api.makeMove(player!!.id, card = player!!.hand[0], targetPlayerId = 1)
                        message = res.ok ?: res.error
                        gameState = api.getState()
                    }
                }) {
                    Text("Wykonaj ruch")
                }

                Spacer(Modifier.height(16.dp))

                Button(onClick = {
                    coroutineScope.launch {
                        val card = api.drawCard(player!!.id)
                        message = "Dobrałeś kartę: ${card.name}"
                        val hand = api.getHand(player!!.id)
                        player = player!!.copy(hand = hand)
                    }
                }) {
                    Text("Dobierz kartę")
                }
            }

            Spacer(Modifier.height(16.dp))


            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        }
    }
}*/

