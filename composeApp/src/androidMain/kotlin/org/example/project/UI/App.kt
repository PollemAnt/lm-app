package org.example.project.UI

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.UI.screens.GameScreen
import org.example.project.UI.screens.LoginScreen
import org.example.project.UI.screens.viewModel.GameViewModel


@Composable
fun App(viewModel: GameViewModel = viewModel()) {

    val state by viewModel.uiState.collectAsState()

    viewModel.loadInitialState()

    if (state.player == null) {
        LoginScreen(onJoin = { name -> viewModel.join(name) })
    } else {
        GameScreen(viewModel = viewModel)
    }
}



