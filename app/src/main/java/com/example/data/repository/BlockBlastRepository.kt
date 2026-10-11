package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MultiplayerRoom
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale
import kotlin.random.Random

class BlockBlastRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val TAG = "BlockBlastRepo"
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("users").document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Failed to observe profile: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        if (profile.userId.isBlank()) return
        try {
            val data = mapOf(
                "userId" to profile.userId,
                "displayName" to profile.displayName,
                "bestScore" to profile.bestScore,
                "currentLevel" to profile.currentLevel,
                "totalLinesCleared" to profile.totalLinesCleared,
                "themeId" to profile.themeId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(profile.userId).set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
        }
    }

    suspend fun submitScore(
        userId: String,
        displayName: String,
        score: Long,
        mode: String,
        lines: Long
    ) {
        if (userId.isBlank() || score <= 0) return
        try {
            val docId = "${userId}_$mode"
            val docRef = db.collection("leaderboard").document(docId)
            val currentDoc = try { docRef.get().await() } catch (_: Exception) { null }
            val existingScore = currentDoc?.getLong("score") ?: 0L

            if (score >= existingScore) {
                val data = mapOf(
                    "userId" to userId,
                    "displayName" to displayName,
                    "score" to score,
                    "mode" to mode,
                    "linesCleared" to lines,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(data).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting score to leaderboard", e)
        }
    }

    fun observeLeaderboard(mode: String): Flow<List<LeaderboardEntry>> = callbackFlow {
        val query = db.collection("leaderboard")
            .whereEqualTo("mode", mode)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(50)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing leaderboard: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                trySend(list)
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun createMultiplayerRoom(hostId: String, hostName: String): String? {
        if (hostId.isBlank()) return null
        return try {
            val roomId = "ROOM-${Random.nextInt(1000, 9999)}"
            val docRef = db.collection("multiplayer_rooms").document(roomId)
            val data = mapOf(
                "id" to roomId,
                "hostId" to hostId,
                "hostName" to hostName,
                "guestId" to "",
                "guestName" to "",
                "status" to "waiting",
                "hostScore" to 0L,
                "guestScore" to 0L,
                "hostLines" to 0L,
                "guestLines" to 0L,
                "hostAlive" to true,
                "guestAlive" to true,
                "winnerId" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
            roomId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating room", e)
            null
        }
    }

    suspend fun joinMultiplayerRoom(roomId: String, guestId: String, guestName: String): Boolean {
        if (roomId.isBlank() || guestId.isBlank()) return false
        val cleanCode = roomId.trim().uppercase(Locale.ROOT)
        return try {
            val docRef = db.collection("multiplayer_rooms").document(cleanCode)
            val snap = docRef.get().await()
            if (!snap.exists()) return false
            val status = snap.getString("status")
            val currentGuest = snap.getString("guestId") ?: ""
            if (status == "waiting" || currentGuest == guestId) {
                val updates = mapOf(
                    "guestId" to guestId,
                    "guestName" to guestName,
                    "status" to "playing",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updates).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room", e)
            false
        }
    }

    fun observeRoom(roomId: String): Flow<MultiplayerRoom?> = callbackFlow {
        if (roomId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val cleanCode = roomId.trim().uppercase(Locale.ROOT)
        val docRef = db.collection("multiplayer_rooms").document(cleanCode)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing room: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val room = snapshot.toObject(MultiplayerRoom::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(room)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateRoomStatus(
        roomId: String,
        isHost: Boolean,
        score: Long,
        lines: Long,
        isAlive: Boolean,
        winnerId: String = ""
    ) {
        if (roomId.isBlank()) return
        val cleanCode = roomId.trim().uppercase(Locale.ROOT)
        val docRef = db.collection("multiplayer_rooms").document(cleanCode)
        try {
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isHost) {
                updates["hostScore"] = score
                updates["hostLines"] = lines
                updates["hostAlive"] = isAlive
            } else {
                updates["guestScore"] = score
                updates["guestLines"] = lines
                updates["guestAlive"] = isAlive
            }
            if (winnerId.isNotBlank()) {
                updates["winnerId"] = winnerId
                updates["status"] = "finished"
            }
            docRef.update(updates).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating room status", e)
        }
    }

    suspend fun leaveRoom(roomId: String, isHost: Boolean) {
        if (roomId.isBlank()) return
        val cleanCode = roomId.trim().uppercase(Locale.ROOT)
        val docRef = db.collection("multiplayer_rooms").document(cleanCode)
        try {
            if (isHost) {
                docRef.delete().await()
            } else {
                docRef.update(
                    mapOf(
                        "guestId" to "",
                        "guestName" to "",
                        "status" to "waiting",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving room", e)
        }
    }
}
