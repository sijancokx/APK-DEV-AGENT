package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GameMode
import com.example.ui.game.BoardCanvas3D
import com.example.ui.game.PieceDock
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val hoveredPiece by viewModel.hoveredPiece.collectAsState()
    val hoverCoord by viewModel.hoverCoord.collectAsState()
    val engine = viewModel.engine

    var boardBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var boardSizePx by remember { mutableStateOf(Pair(0f, 0f)) }

    BackHandler {
        viewModel.navigateTo(ScreenDestination.HOME)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.boardBackground))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top HUD (Navigation, Score, High Score, Combo)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0x33000000))
                            .testTag("back_to_home_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = Color.White
                        )
                    }

                    // Score Display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SKOR",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${engine.score}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Best Score Display
                    Surface(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${engine.highScore}",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Combo Streak Glow Banner
                AnimatedVisibility(
                    visible = engine.comboStreak > 1,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "combo_pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = theme.glowColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.glowColor),
                            modifier = Modifier.scale(pulseScale)
                        ) {
                            Text(
                                text = "🔥 COMBO x${engine.comboStreak}! BLAST BONUS",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Mode-Specific Headers (Timer for Time Attack, Objectives for Levels)
                if (engine.currentMode == GameMode.TIME_ATTACK) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Sisa Waktu",
                            tint = if (engine.remainingTimeSeconds <= 15) Color.Red else theme.glowColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${engine.remainingTimeSeconds} Detik",
                            color = if (engine.remainingTimeSeconds <= 15) Color.Red else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (engine.remainingTimeSeconds / 90f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (engine.remainingTimeSeconds <= 15) Color.Red else theme.glowColor,
                        trackColor = Color(0x33FFFFFF)
                    )
                } else if (engine.currentMode == GameMode.LEVELS || engine.currentMode == GameMode.DAILY_CHALLENGE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Baris: ${engine.levelLinesCurrent}/${engine.levelLinesTarget}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Target Skor: ${engine.score}/${engine.levelTargetScore}",
                            color = theme.glowColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. 3D Game Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .onGloballyPositioned { coordinates ->
                        boardBoundsInRoot = coordinates.boundsInRoot()
                        boardSizePx = Pair(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
                    },
                contentAlignment = Alignment.Center
            ) {
                BoardCanvas3D(
                    engine = engine,
                    theme = theme,
                    hoveredPiece = hoveredPiece,
                    hoverCoord = hoverCoord,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Piece Tray Dock
            PieceDock(
                pieces = engine.availablePieces,
                theme = theme,
                boardBoundsInRoot = boardBoundsInRoot,
                onHoverChange = { piece, coord ->
                    viewModel.setHover(piece, coord)
                },
                onPlacePiece = { index, row, col ->
                    val placed = viewModel.placePiece(
                        pieceIndex = index,
                        row = row,
                        col = col,
                        boardWidthPx = boardSizePx.first,
                        boardHeightPx = boardSizePx.second
                    )
                    placed
                },
                onPickup = {
                    viewModel.soundHaptic?.playPickup()
                },
                modifier = Modifier.fillMaxWidth().testTag("piece_dock")
            )
        }

        // 4. Game Over / Level Complete Dialog
        if (engine.isGameOver || engine.isLevelCompleted) {
            val isWin = engine.isLevelCompleted
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cellBackground),
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (isWin) Color(0xFFFFD700) else theme.gridBorder),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("game_result_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isWin) "🎉 LEVEL SELESAI!" else "GAME OVER",
                            color = if (isWin) Color(0xFFFFD700) else Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = if (isWin) "Luar biasa! Target level tercapai." else "Tidak ada ruang tersisa untuk blok berikutnya.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Surface(
                            color = Color(0x33000000),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "SKOR AKHIR",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${engine.score}",
                                    color = Color.White,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Baris Dihancurkan: ${engine.linesClearedTotal}",
                                    color = theme.glowColor,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("dialog_home_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FFFFFF)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menu", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    when (engine.currentMode) {
                                        GameMode.CLASSIC -> viewModel.startClassicGame()
                                        GameMode.TIME_ATTACK -> viewModel.startTimeAttackGame()
                                        GameMode.LEVELS -> viewModel.startLevelGame(if (isWin) engine.currentLevel + 1 else engine.currentLevel)
                                        GameMode.DAILY_CHALLENGE -> viewModel.startDailyGame()
                                        GameMode.MULTIPLAYER -> viewModel.navigateTo(ScreenDestination.MULTIPLAYER_LOBBY)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("dialog_replay_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.palette.first()),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isWin) "Lanjut" else "Ulangi", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
