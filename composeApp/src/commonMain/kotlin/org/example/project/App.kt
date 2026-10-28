package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.ui.screens.GameScreen
import org.example.project.ui.screens.LoginScreen
import org.example.project.ui.screens.Settings
import org.example.project.ui.screens.viewModel.GameViewModel
import org.koin.compose.viewmodel.koinViewModel
import ui.theme.AppColorScheme

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Game : Screen("game")
    data object Settings : Screen("settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: GameViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showSettingsIcon = currentRoute in listOf(Screen.Login.route, Screen.Game.route)

    val screenTitle = when (currentRoute) {
        Screen.Login.route -> "Logowanie"
        Screen.Game.route -> "Love Letter"
        Screen.Settings.route -> "Ustawienia"
        else -> "Love Letter"
    }

    LaunchedEffect(Unit) {
        val savedPlayerId = viewModel.getSavedPlayerId()
        if (savedPlayerId != null) {
            viewModel.rejoin(
                playerId = savedPlayerId,
                onSuccess = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onError = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        } else {
            viewModel.loadInitialState()
        }
    }

    MaterialTheme(
        colorScheme = AppColorScheme
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = screenTitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                               .padding(end = 8.dp)
                        ) {
                            val connectionStatus = state.isConnected ?: "Rozłączony"
                            val isConnected = state.isConnected != null

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isConnected) Green
                                            else Red
                                        )
                                )
                                Text(
                                    text = connectionStatus,
                                    fontSize = 12.sp,
                                    color = if (isConnected) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (showSettingsIcon) {
                            IconButton(
                                onClick = { navController.navigate(Screen.Settings.route) },
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Ustawienia",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.secondary,
                        actionIconContentColor = MaterialTheme.colorScheme.secondary
                    )

                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (state.isRestoringSession) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Wznawianie sesji gry...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                state = state,
                                onJoin = { name ->
                                     viewModel.join(name) {
                                    navController.navigate(Screen.Game.route)
                                     }
                                }
                            )
                        }

                        composable(Screen.Game.route) {
                            if (state.player == null) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }

                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator()
                                        Text("Przekierowywanie do logowania...")
                                    }
                                }
                            } else {
                                GameScreen(viewModel = viewModel)
                            }
                        }

                        composable(Screen.Settings.route) {
                            Settings(
                                onReset = {
                                    viewModel.reset()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
