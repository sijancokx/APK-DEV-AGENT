package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.data.repository.BlockBlastRepository
import com.example.ui.screens.DailyChallengeScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelMapScreen
import com.example.ui.screens.MultiplayerScreen
import com.example.ui.screens.ThemeSelectorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SoundHapticEffect
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = BlockBlastRepository(this)
        val hapticEffect = SoundHapticEffect(this)
        val authManager = AuthManager(this)

        setContent {
            MyApplicationTheme {
                val coroutineScope = rememberCoroutineScope()
                val viewModel: GameViewModel = viewModel {
                    GameViewModel(repository, hapticEffect)
                }

                // Attempt silent auto sign in on startup
                LaunchedEffect(Unit) {
                    authManager.attemptSilentAutoSignIn(coroutineScope)
                }

                // Synchronize auth state with game viewmodel
                val currentUser by authManager.currentUser.collectAsState()
                LaunchedEffect(currentUser) {
                    viewModel.setAuthenticatedUser(currentUser?.uid, currentUser?.displayName)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlockBlastApp(
                        viewModel = viewModel,
                        authManager = authManager
                    )
                }
            }
        }
    }
}

@Composable
fun BlockBlastApp(
    viewModel: GameViewModel,
    authManager: AuthManager,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.screen.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            ScreenDestination.HOME -> {
                HomeScreen(viewModel = viewModel, authManager = authManager)
            }
            ScreenDestination.GAME_PLAY -> {
                GameScreen(viewModel = viewModel)
            }
            ScreenDestination.LEVELS_MAP -> {
                LevelMapScreen(viewModel = viewModel)
            }
            ScreenDestination.DAILY_CHALLENGE -> {
                DailyChallengeScreen(viewModel = viewModel)
            }
            ScreenDestination.MULTIPLAYER_LOBBY,
            ScreenDestination.MULTIPLAYER_GAME -> {
                MultiplayerScreen(viewModel = viewModel)
            }
            ScreenDestination.LEADERBOARD -> {
                LeaderboardScreen(viewModel = viewModel)
            }
            ScreenDestination.THEME_PICKER -> {
                ThemeSelectorScreen(viewModel = viewModel)
            }
        }
    }
}
