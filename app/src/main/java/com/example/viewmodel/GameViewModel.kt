package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MultiplayerRoom
import com.example.data.model.UserProfile
import com.example.data.repository.BlockBlastRepository
import com.example.engine.BlockBlastEngine
import com.example.model.BlockPiece
import com.example.model.GameMode
import com.example.model.ThemeType
import com.example.util.SoundHapticEffect
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenDestination {
    HOME,
    GAME_PLAY,
    LEVELS_MAP,
    DAILY_CHALLENGE,
    MULTIPLAYER_LOBBY,
    MULTIPLAYER_GAME,
    LEADERBOARD,
    THEME_PICKER
}

class GameViewModel(
    private val repository: BlockBlastRepository,
    var soundHaptic: SoundHapticEffect? = null
) : ViewModel() {

    val engine = BlockBlastEngine()

    private val _screen = MutableStateFlow(ScreenDestination.HOME)
    val screen: StateFlow<ScreenDestination> = _screen.asStateFlow()

    private val _currentTheme = MutableStateFlow(ThemeType.CYBERPUNK)
    val currentTheme: StateFlow<ThemeType> = _currentTheme.asStateFlow()

    private val _hoveredPiece = MutableStateFlow<BlockPiece?>(null)
    val hoveredPiece: StateFlow<BlockPiece?> = _hoveredPiece.asStateFlow()

    private val _hoverCoord = MutableStateFlow<Pair<Int, Int>?>(null)
    val hoverCoord: StateFlow<Pair<Int, Int>?> = _hoverCoord.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    private val _activeRoom = MutableStateFlow<MultiplayerRoom?>(null)
    val activeRoom: StateFlow<MultiplayerRoom?> = _activeRoom.asStateFlow()

    var isRoomHost: Boolean = false
    var currentRoomCode: String = ""
    var currentUserId: String = "guest_player"
    var currentDisplayName: String = "Pemain Blok"

    private var gameLoopJob: Job? = null
    private var timerJob: Job? = null
    private var roomObservationJob: Job? = null

    init {
        // Start game physics ticker
        startGameLoop()
    }

    fun setAuthenticatedUser(userId: String?, displayName: String?) {
        val uid = userId ?: "guest_player"
        val name = displayName ?: "Pemain"
        currentUserId = uid
        currentDisplayName = name

        if (userId != null) {
            viewModelScope.launch {
                repository.observeUserProfile(uid)
                    .catch { emit(null) }
                    .collect { profile ->
                        if (profile != null) {
                            _userProfile.value = profile
                            engine.highScore = profile.bestScore
                            // Load saved theme
                            try {
                                _currentTheme.value = ThemeType.valueOf(profile.themeId)
                            } catch (_: Exception) {}
                        } else {
                            // Create new initial profile
                            val newProfile = UserProfile(
                                userId = uid,
                                displayName = name,
                                bestScore = 0L,
                                currentLevel = 1L,
                                totalLinesCleared = 0L,
                                themeId = _currentTheme.value.name
                            )
                            repository.saveUserProfile(newProfile)
                            _userProfile.value = newProfile
                        }
                    }
            }
        }
    }

    fun navigateTo(dest: ScreenDestination) {
        _screen.value = dest
        if (dest == ScreenDestination.LEADERBOARD) {
            fetchLeaderboard("classic")
        }
    }

    fun selectTheme(theme: ThemeType) {
        _currentTheme.value = theme
        _userProfile.value?.let { prof ->
            val updated = prof.copy(themeId = theme.name)
            _userProfile.value = updated
            viewModelScope.launch {
                repository.saveUserProfile(updated)
            }
        }
    }

    fun startClassicGame() {
        engine.startNewGame(GameMode.CLASSIC, initialHighScore = engine.highScore)
        _screen.value = ScreenDestination.GAME_PLAY
    }

    fun startTimeAttackGame() {
        engine.startNewGame(GameMode.TIME_ATTACK, initialHighScore = engine.highScore)
        startTimerTicker()
        _screen.value = ScreenDestination.GAME_PLAY
    }

    fun startLevelGame(level: Int) {
        engine.startNewGame(GameMode.LEVELS, level = level, initialHighScore = engine.highScore)
        _screen.value = ScreenDestination.GAME_PLAY
    }

    fun startDailyGame() {
        engine.startNewGame(GameMode.DAILY_CHALLENGE, initialHighScore = engine.highScore)
        _screen.value = ScreenDestination.GAME_PLAY
    }

    fun setHover(piece: BlockPiece?, coord: Pair<Int, Int>?) {
        _hoveredPiece.value = piece
        _hoverCoord.value = coord
    }

    fun placePiece(pieceIndex: Int, row: Int, col: Int, boardWidthPx: Float, boardHeightPx: Float): Boolean {
        val success = engine.placePiece(
            pieceIndex = pieceIndex,
            startRow = row,
            startCol = col,
            boardWidthPx = boardWidthPx,
            boardHeightPx = boardHeightPx,
            currentTheme = _currentTheme.value,
            haptic = soundHaptic
        )

        if (success) {
            // Update profile stats if authenticated
            checkSaveScores()

            // If in multiplayer game, push score and lines in real-time
            if (_screen.value == ScreenDestination.MULTIPLAYER_GAME && currentRoomCode.isNotBlank()) {
                viewModelScope.launch {
                    repository.updateRoomStatus(
                        roomId = currentRoomCode,
                        isHost = isRoomHost,
                        score = engine.score,
                        lines = engine.linesClearedTotal,
                        isAlive = !engine.isGameOver
                    )
                }
            }
        }

        return success
    }

    private fun checkSaveScores() {
        val profile = _userProfile.value ?: return
        val newBest = maxOf(profile.bestScore, engine.score)
        val newLines = profile.totalLinesCleared + (if (engine.linesClearedTotal > 0) 1 else 0)
        val newLevel = if (engine.isLevelCompleted) profile.currentLevel + 1 else profile.currentLevel

        val updated = profile.copy(
            bestScore = newBest,
            totalLinesCleared = newLines,
            currentLevel = newLevel
        )
        _userProfile.value = updated

        viewModelScope.launch {
            repository.saveUserProfile(updated)
            // Submit to leaderboard if mode finished or highscore
            val modeStr = when (engine.currentMode) {
                GameMode.CLASSIC -> "classic"
                GameMode.TIME_ATTACK -> "time_attack"
                GameMode.DAILY_CHALLENGE -> "daily"
                else -> "classic"
            }
            repository.submitScore(
                userId = currentUserId,
                displayName = currentDisplayName,
                score = engine.score,
                mode = modeStr,
                lines = engine.linesClearedTotal
            )
        }
    }

    private fun startTimerTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && engine.currentMode == GameMode.TIME_ATTACK && !engine.isGameOver) {
                delay(1000L)
                engine.tickTimer(soundHaptic)
                if (engine.isGameOver) {
                    checkSaveScores()
                }
            }
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                val dt = ((now - lastTime) / 1000f).coerceIn(0.005f, 0.05f)
                lastTime = now

                engine.updatePhysics(dt)
                delay(16L) // ~60fps
            }
        }
    }

    fun fetchLeaderboard(mode: String) {
        viewModelScope.launch {
            repository.observeLeaderboard(mode)
                .catch { emit(emptyList()) }
                .collect { list ->
                    _leaderboardEntries.value = list
                }
        }
    }

    // Multiplayer actions
    fun createMultiplayerRoom(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val code = repository.createMultiplayerRoom(currentUserId, currentDisplayName)
            if (code != null) {
                isRoomHost = true
                currentRoomCode = code
                observeCurrentRoom(code)
                onSuccess(code)
            } else {
                onError("Gagal membuat room. Periksa koneksi internet.")
            }
        }
    }

    fun joinMultiplayerRoom(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val joined = repository.joinMultiplayerRoom(code, currentUserId, currentDisplayName)
            if (joined) {
                isRoomHost = false
                currentRoomCode = code
                observeCurrentRoom(code)
                onSuccess()
            } else {
                onError("Room tidak ditemukan atau sudah penuh.")
            }
        }
    }

    private fun observeCurrentRoom(roomId: String) {
        roomObservationJob?.cancel()
        roomObservationJob = viewModelScope.launch {
            repository.observeRoom(roomId)
                .catch { emit(null) }
                .collect { room ->
                    _activeRoom.value = room
                    if (room != null && room.status == "playing" && _screen.value == ScreenDestination.MULTIPLAYER_LOBBY) {
                        // Start multiplayer battle
                        engine.startNewGame(GameMode.MULTIPLAYER)
                        _screen.value = ScreenDestination.MULTIPLAYER_GAME
                    }
                }
        }
    }

    fun leaveMultiplayer() {
        roomObservationJob?.cancel()
        if (currentRoomCode.isNotBlank()) {
            viewModelScope.launch {
                repository.leaveRoom(currentRoomCode, isRoomHost)
                currentRoomCode = ""
                _activeRoom.value = null
            }
        }
        _screen.value = ScreenDestination.HOME
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        timerJob?.cancel()
        roomObservationJob?.cancel()
    }
}
