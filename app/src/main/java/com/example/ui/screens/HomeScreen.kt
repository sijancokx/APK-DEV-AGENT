package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.model.ThemeType
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    authManager: AuthManager,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    val isSigningIn by authManager.isSigningIn.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.boardBackground))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Profile & Sign In Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("profile_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cellBackground.copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.gridBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(theme.palette.first()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.displayName?.take(1) ?: "P"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser?.displayName ?: "Pemain Tamu",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Skor Terbaik: ${userProfile?.bestScore ?: viewModel.engine.highScore}",
                                    color = theme.glowColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (currentUser == null) {
                            Button(
                                onClick = {
                                    (context as? Activity)?.let { act ->
                                        authManager.signInWithGoogle(act, scope)
                                    }
                                },
                                enabled = !isSigningIn,
                                colors = ButtonDefaults.buttonColors(containerColor = theme.palette[1]),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("google_sign_in_button")
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Masuk", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { authManager.signOut(scope) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Text("Keluar", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // 2. Hero Game Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startClassicGame() }
                        .testTag("hero_play_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(theme.palette[0].copy(alpha = 0.85f), theme.palette[1].copy(alpha = 0.95f))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BLOCK BLAST 3D",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mode Klasik Tanpa Batas Waktu",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.startClassicGame() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.testTag("main_play_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Main Sekarang", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = "Main",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Game Modes Section Title
            item {
                Text(
                    text = "Mode Permainan",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )
            }

            // Time Attack Mode Card
            item {
                GameModeCard(
                    title = "Tantangan Waktu",
                    description = "90 detik balapan skor! Hancurkan baris untuk tambah waktu.",
                    tag = "Waktu Cepat",
                    icon = Icons.Default.Timer,
                    accentColor = theme.palette[2],
                    testTag = "time_attack_card",
                    onClick = { viewModel.startTimeAttackGame() }
                )
            }

            // Real-time Multiplayer 1v1 Battle Card
            item {
                GameModeCard(
                    title = "Duel 1v1 Daring",
                    description = "Tantang pemain lain secara real-time! Serang dengan blok sampah.",
                    tag = "Multipemain",
                    icon = Icons.Default.Group,
                    accentColor = theme.palette[3],
                    testTag = "multiplayer_card",
                    onClick = { viewModel.navigateTo(ScreenDestination.MULTIPLAYER_LOBBY) }
                )
            }

            // Levels Adventure Card
            item {
                GameModeCard(
                    title = "Petualangan Level",
                    description = "Taklukkan 20+ tingkat rintangan beku dan target bertahap.",
                    tag = "Tingkat ${userProfile?.currentLevel ?: 1}",
                    icon = Icons.Default.Star,
                    accentColor = theme.palette[4],
                    testTag = "levels_card",
                    onClick = { viewModel.navigateTo(ScreenDestination.LEVELS_MAP) }
                )
            }

            // Daily Challenge Card
            item {
                GameModeCard(
                    title = "Tantangan Harian",
                    description = "Teka-teki unik setiap hari dengan lencana trofi eksklusif.",
                    tag = "Hari Ini",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = theme.palette[0],
                    testTag = "daily_challenge_card",
                    onClick = { viewModel.navigateTo(ScreenDestination.DAILY_CHALLENGE) }
                )
            }

            // 4. Bottom Menu Actions (Leaderboard & Themes)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.LEADERBOARD) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("leaderboard_nav_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cellBackground),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.gridBorder)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Papan Peringkat", tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Peringkat", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(ScreenDestination.THEME_PICKER) },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("themes_nav_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.cellBackground),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.gridBorder)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Kustomisasi Tema", tint = theme.glowColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tema 3D", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    description: String,
    tag: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = accentColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = tag,
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
