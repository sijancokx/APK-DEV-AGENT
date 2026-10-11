package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ScreenDestination

@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val entries by viewModel.leaderboardEntries.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val modes = listOf("classic" to "Klasik", "time_attack" to "Waktu", "daily" to "Harian")

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
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.HOME) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000))
                        .testTag("leaderboard_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Papan Peringkat Daring",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mode Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = theme.cellBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = theme.glowColor,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("leaderboard_tabs")
            ) {
                modes.forEachIndexed { index, pair ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            viewModel.fetchLeaderboard(pair.first)
                        },
                        text = {
                            Text(
                                text = pair.second,
                                color = if (selectedTabIndex == index) Color.White else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum ada skor tercatat.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Jadilah yang pertama mencetak rekor global!",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(entries) { index, item ->
                        LeaderboardItemRow(index = index + 1, entry = item, themeColor = theme.glowColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardItemRow(
    index: Int,
    entry: LeaderboardEntry,
    themeColor: Color
) {
    val rankBadgeColor = when (index) {
        1 -> Color(0xFFFFD700) // Emas
        2 -> Color(0xFFC0C0C0) // Perak
        3 -> Color(0xFFCD7F32) // Perunggu
        else -> Color.White.copy(alpha = 0.4f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (index <= 3) rankBadgeColor.copy(alpha = 0.5f) else Color(0x22FFFFFF)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(rankBadgeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (index <= 3) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = rankBadgeColor, modifier = Modifier.size(20.dp))
                    } else {
                        Text(text = "$index", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = entry.displayName.ifBlank { "Pemain" },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${entry.linesCleared} Baris Bersih",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "${entry.score}",
                color = if (index <= 3) rankBadgeColor else themeColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
