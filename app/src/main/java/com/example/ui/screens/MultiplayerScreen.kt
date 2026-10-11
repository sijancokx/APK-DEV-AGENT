package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.game.BoardCanvas3D
import com.example.ui.game.PieceDock
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun MultiplayerScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val screen by viewModel.screen.collectAsState()
    val context = LocalContext.current

    BackHandler {
        viewModel.leaveMultiplayer()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.boardBackground))
    ) {
        if (screen == ScreenDestination.MULTIPLAYER_LOBBY) {
            MultiplayerLobbyContent(viewModel, context)
        } else {
            MultiplayerBattleContent(viewModel)
        }
    }
}

@Composable
private fun MultiplayerLobbyContent(
    viewModel: GameViewModel,
    context: Context
) {
    val theme by viewModel.currentTheme.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    var inputCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.leaveMultiplayer() },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000))
                    .testTag("multiplayer_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Duel Multipemain 1v1",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Lobby Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (activeRoom == null) {
                // Host a Room Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cellBackground),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.gridBorder),
                    modifier = Modifier.fillMaxWidth().testTag("create_room_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SportsKabaddi,
                            contentDescription = null,
                            tint = theme.palette.first(),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Buat Pertandingan Baru",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dapatkan kode room dan ajak teman untuk berduel langsung.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                viewModel.createMultiplayerRoom(
                                    onSuccess = { isLoading = false },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.palette.first()),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_create_room")
                        ) {
                            Text("Buat Room Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Divider Or
                Text(text = "— ATAU GABUNG DENGAN KODE —", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                // Join Room Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cellBackground),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.gridBorder),
                    modifier = Modifier.fillMaxWidth().testTag("join_room_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it.uppercase() },
                            label = { Text("Masukkan Kode Room (cth: ROOM-1234)") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = theme.glowColor,
                                unfocusedBorderColor = Color(0x66FFFFFF),
                                focusedLabelColor = theme.glowColor,
                                unfocusedLabelColor = Color(0xAAFFFFFF)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("room_code_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (inputCode.isNotBlank()) {
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.joinMultiplayerRoom(
                                        code = inputCode,
                                        onSuccess = { isLoading = false },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading && inputCode.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.palette[1]),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_join_room")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Gabung Duel", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Waiting for Guest inside Room
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cellBackground),
                    border = androidx.compose.foundation.BorderStroke(2.dp, theme.glowColor),
                    modifier = Modifier.fillMaxWidth().testTag("waiting_room_card")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = theme.glowColor, modifier = Modifier.size(48.dp))

                        Text(
                            text = "Menunggu Lawan Bergabung...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = Color(0x44000000),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Kode Pertandingan:", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                    Text(activeRoom?.id ?: viewModel.currentRoomCode, color = theme.glowColor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Room Code", activeRoom?.id ?: viewModel.currentRoomCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Kode room disalin!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Color.White)
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.leaveMultiplayer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x44FF0000)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Batalkan Room", color = Color.White)
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(text = errorMessage ?: "", color = Color.Red, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MultiplayerBattleContent(
    viewModel: GameViewModel
) {
    val theme by viewModel.currentTheme.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val hoveredPiece by viewModel.hoveredPiece.collectAsState()
    val hoverCoord by viewModel.hoverCoord.collectAsState()
    val engine = viewModel.engine

    var boardBoundsInRoot by remember { mutableStateOf<Rect?>(null) }
    var boardSizePx by remember { mutableStateOf(Pair(0f, 0f)) }

    val isHost = viewModel.isRoomHost
    val myScore = engine.score
    val myLines = engine.linesClearedTotal

    val opponentName = if (isHost) activeRoom?.guestName ?: "Lawan" else activeRoom?.hostName ?: "Lawan"
    val opponentScore = if (isHost) activeRoom?.guestScore ?: 0L else activeRoom?.hostScore ?: 0L
    val opponentLines = if (isHost) activeRoom?.guestLines ?: 0L else activeRoom?.hostLines ?: 0L
    val opponentAlive = if (isHost) activeRoom?.guestAlive ?: true else activeRoom?.hostAlive ?: true

    val isMatchFinished = engine.isGameOver || !opponentAlive || activeRoom?.status == "finished"
    val iWon = (!opponentAlive && !engine.isGameOver) || (engine.isGameOver && !opponentAlive && myScore >= opponentScore) || (activeRoom?.winnerId == viewModel.currentUserId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top 1v1 Split Battle HUD
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = theme.cellBackground),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, theme.gridBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // You
                Column(horizontalAlignment = Alignment.Start) {
                    Text("KAMU", color = theme.palette.first(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$myScore", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("$myLines Baris", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }

                // VS Badge
                Surface(
                    color = Color.Red.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Text("VS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }

                // Opponent
                Column(horizontalAlignment = Alignment.End) {
                    Text(opponentName, color = theme.palette[1], fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$opponentScore", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(if (opponentAlive) "$opponentLines Baris" else "TERELIMINASI", color = if (opponentAlive) Color.White.copy(alpha = 0.7f) else Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Center 3D Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
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

        // Bottom Pieces Dock
        PieceDock(
            pieces = engine.availablePieces,
            theme = theme,
            boardBoundsInRoot = boardBoundsInRoot,
            onHoverChange = { piece, coord -> viewModel.setHover(piece, coord) },
            onPlacePiece = { index, row, col ->
                viewModel.placePiece(
                    pieceIndex = index,
                    row = row,
                    col = col,
                    boardWidthPx = boardSizePx.first,
                    boardHeightPx = boardSizePx.second
                )
            },
            onPickup = { viewModel.soundHaptic?.playPickup() },
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Match Result Dialog
    if (isMatchFinished) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cellBackground),
                border = androidx.compose.foundation.BorderStroke(2.dp, if (iWon) Color(0xFFFFD700) else Color.Red),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (iWon) "🏆 KEMENANGAN TELAK!" else "KALAH DALAM DUEL",
                        color = if (iWon) Color(0xFFFFD700) else Color.Red,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (iWon) "Kamu mendominasi papan duel 1v1!" else "Lawan berhasil bertahan lebih lama kali ini.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Skormu", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("$myScore", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Skor Lawan", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("$opponentScore", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.leaveMultiplayer() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.palette.first()),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Kembali ke Beranda", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
