package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun LevelMapScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val playerCurrentLevel = (userProfile?.currentLevel ?: 1L).toInt()

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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000))
                        .testTag("levels_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Petualangan Level",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Taklukkan tingkat rintangan dan capai Level 20+",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(20) { index ->
                    val level = index + 1
                    val isUnlocked = level <= playerCurrentLevel
                    val isCompleted = level < playerCurrentLevel

                    LevelTile(
                        level = level,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        themeColor = theme.palette.first(),
                        onClick = {
                            if (isUnlocked) {
                                viewModel.startLevelGame(level)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelTile(
    level: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(76.dp)
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_tile_$level"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> themeColor.copy(alpha = 0.25f)
                isUnlocked -> themeColor
                else -> Color(0x22000000)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isUnlocked) themeColor else Color(0x33FFFFFF)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Terkunci",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$level",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row {
                        repeat(3) { starIndex ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isCompleted) Color(0xFFFFD700) else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
