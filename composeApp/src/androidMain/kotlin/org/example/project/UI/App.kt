package org.example.project.UI

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.UI.screens.GameScreen
import org.example.project.UI.screens.LoginScreen
import org.example.project.UI.screens.Settings
import org.example.project.UI.screens.viewModel.GameViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Game : Screen("game")
    object Settings : Screen("settings")
}

@Composable
fun App(viewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val showSettingsIcon = currentDestination?.route in listOf(
        Screen.Login.route,
        Screen.Game.route
    )
    viewModel.loadInitialState()

    Text(
        state.isConnected ?: "Brak połączenia z serwerem",
        modifier = Modifier
            .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    )

    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onJoin = { name ->
                    viewModel.join(name){ player ->
                        navController.navigate(Screen.Game.route)
                    }
                })
        }
        composable(Screen.Game.route) {
            if (state.player == null) {
                navController.navigate(Screen.Login.route)
                return@composable
            }
            println("[DEBUG] App GameScreen: $state, $viewModel , ${state.player}")
            GameScreen(viewModel = viewModel)
        }
        composable(Screen.Settings.route) {
            Settings()
        }
    }

    if (showSettingsIcon) {
        IconButton(
            onClick = {
                navController.navigate(Screen.Settings.route)
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ustawienia",
                tint = Color.Black,
                modifier = Modifier.size(60.dp)
            )
        }
    }
}




