package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String = "Pemain",
    val bestScore: Long = 0L,
    val currentLevel: Long = 1L,
    val totalLinesCleared: Long = 0L,
    val themeId: String = "CYBERPUNK",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Pemain",
    val score: Long = 0L,
    val mode: String = "classic", // "classic", "time_attack", "daily"
    val linesCleared: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MultiplayerRoom(
    @DocumentId val id: String = "",
    val hostId: String = "",
    val hostName: String = "Tuan Rumah",
    val guestId: String = "",
    val guestName: String = "",
    val status: String = "waiting", // "waiting", "playing", "finished"
    val hostScore: Long = 0L,
    val guestScore: Long = 0L,
    val hostLines: Long = 0L,
    val guestLines: Long = 0L,
    val hostAlive: Boolean = true,
    val guestAlive: Boolean = true,
    val winnerId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
