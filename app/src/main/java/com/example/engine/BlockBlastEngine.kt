package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.model.BlockPiece
import com.example.model.BlockShapes
import com.example.model.CellCoord
import com.example.model.FloatingScore
import com.example.model.GameMode
import com.example.model.Particle3D
import com.example.model.PlacedCell
import com.example.model.ShockwaveRing
import com.example.model.ThemeType
import com.example.util.SoundHapticEffect
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class BlockBlastEngine {
    val gridSize = 8

    val grid = Array(gridSize) { Array<PlacedCell?>(gridSize) { null } }
    var availablePieces = mutableListOf<BlockPiece?>()

    var score: Long = 0L
    var highScore: Long = 0L
    var comboStreak: Int = 0
    var linesClearedTotal: Long = 0L
    var isGameOver: Boolean = false

    var currentMode: GameMode = GameMode.CLASSIC
    var remainingTimeSeconds: Int = 90
    var currentLevel: Int = 1
    var levelTargetScore: Long = 1000L
    var levelLinesTarget: Int = 5
    var levelLinesCurrent: Int = 0
    var isLevelCompleted: Boolean = false

    val particles = mutableListOf<Particle3D>()
    val shockwaves = mutableListOf<ShockwaveRing>()
    val floatingScores = mutableListOf<FloatingScore>()

    private var scoreIdCounter = 0L

    fun startNewGame(mode: GameMode, level: Int = 1, initialHighScore: Long = 0L) {
        currentMode = mode
        currentLevel = level
        score = 0L
        highScore = initialHighScore
        comboStreak = 0
        linesClearedTotal = 0L
        isGameOver = false
        isLevelCompleted = false
        levelLinesCurrent = 0
        remainingTimeSeconds = if (mode == GameMode.TIME_ATTACK) 90 else 0

        particles.clear()
        shockwaves.clear()
        floatingScores.clear()

        // Clear grid
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                grid[r][c] = null
            }
        }

        // Setup Level-specific obstacles and targets
        if (mode == GameMode.LEVELS) {
            setupLevelLayout(level)
        } else if (mode == GameMode.DAILY_CHALLENGE) {
            setupDailyChallenge()
        }

        spawnNewPieces()
    }

    private fun setupLevelLayout(level: Int) {
        levelTargetScore = (level * 600L) + 400L
        levelLinesTarget = (3 + level * 2).coerceAtMost(25)

        // Add interesting obstacles for higher levels
        val obstacleCount = (level / 2).coerceAtMost(6)
        val random = Random(level * 31)
        var placed = 0
        while (placed < obstacleCount) {
            val r = random.nextInt(1, 7)
            val c = random.nextInt(1, 7)
            if (grid[r][c] == null) {
                grid[r][c] = PlacedCell(colorIndex = 5, isObstacle = true)
                placed++
            }
        }
    }

    private fun setupDailyChallenge() {
        levelTargetScore = 2500L
        levelLinesTarget = 15
        // Place 3 strategic starter obstacle gems
        grid[2][2] = PlacedCell(colorIndex = 4, isObstacle = true)
        grid[5][5] = PlacedCell(colorIndex = 4, isObstacle = true)
        grid[2][5] = PlacedCell(colorIndex = 4, isObstacle = true)
    }

    fun spawnNewPieces() {
        availablePieces = BlockShapes.getRandomPieces(3).toMutableList()
        checkGameOver()
    }

    fun canPlacePiece(piece: BlockPiece, startRow: Int, startCol: Int): Boolean {
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    if (targetR !in 0 until gridSize || targetC !in 0 until gridSize) {
                        return false
                    }
                    if (grid[targetR][targetC] != null) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun placePiece(
        pieceIndex: Int,
        startRow: Int,
        startCol: Int,
        boardWidthPx: Float,
        boardHeightPx: Float,
        currentTheme: ThemeType,
        haptic: SoundHapticEffect?
    ): Boolean {
        if (pieceIndex !in availablePieces.indices) return false
        val piece = availablePieces[pieceIndex] ?: return false

        if (!canPlacePiece(piece, startRow, startCol)) return false

        // Place on grid
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.shape[r][c]) {
                    val targetR = startRow + r
                    val targetC = startCol + c
                    grid[targetR][targetC] = PlacedCell(colorIndex = piece.colorIndex)
                }
            }
        }

        // Add score for placement (10 points per cell)
        val placementScore = piece.cellCount * 10
        score += placementScore
        if (score > highScore) highScore = score

        haptic?.playSnap()

        // Clear piece from available slot
        availablePieces[pieceIndex] = null

        // Check for completed rows and columns
        val rowsToClear = mutableListOf<Int>()
        val colsToClear = mutableListOf<Int>()

        for (r in 0 until gridSize) {
            if ((0 until gridSize).all { c -> grid[r][c] != null }) {
                rowsToClear.add(r)
            }
        }

        for (c in 0 until gridSize) {
            if ((0 until gridSize).all { r -> grid[r][c] != null }) {
                colsToClear.add(c)
            }
        }

        val totalLines = rowsToClear.size + colsToClear.size
        val cellSizePx = (boardWidthPx / gridSize).coerceAtLeast(1f)

        if (totalLines > 0) {
            comboStreak++
            linesClearedTotal += totalLines
            levelLinesCurrent += totalLines

            // Line clear score bonus with exponential combo
            val lineBaseScore = totalLines * 120
            val comboBonus = comboStreak * 80 * totalLines
            val earnedScore = (lineBaseScore + comboBonus).toLong()
            score += earnedScore
            if (score > highScore) highScore = score

            // Time attack bonus
            if (currentMode == GameMode.TIME_ATTACK) {
                val bonusSeconds = 4 * totalLines + (comboStreak * 2)
                remainingTimeSeconds = (remainingTimeSeconds + bonusSeconds).coerceAtMost(180)
            }

            // Trigger particles for all cleared cells
            val clearedCoords = mutableSetOf<CellCoord>()
            rowsToClear.forEach { r ->
                (0 until gridSize).forEach { c -> clearedCoords.add(CellCoord(r, c)) }
            }
            colsToClear.forEach { c ->
                (0 until gridSize).forEach { r -> clearedCoords.add(CellCoord(r, c)) }
            }

            for (coord in clearedCoords) {
                val cell = grid[coord.row][coord.col]
                val color = if (cell != null && cell.colorIndex in currentTheme.palette.indices) {
                    currentTheme.palette[cell.colorIndex]
                } else {
                    currentTheme.glowColor
                }

                val centerX = (coord.col + 0.5f) * cellSizePx
                val centerY = (coord.row + 0.5f) * cellSizePx

                spawnBlastParticles(centerX, centerY, color, cellSizePx)
                grid[coord.row][coord.col] = null
            }

            // Shockwave in the center of blast
            val avgX = clearedCoords.map { (it.col + 0.5f) * cellSizePx }.average().toFloat()
            val avgY = clearedCoords.map { (it.row + 0.5f) * cellSizePx }.average().toFloat()
            shockwaves.add(
                ShockwaveRing(
                    centerX = avgX,
                    centerY = avgY,
                    maxRadius = cellSizePx * 4f,
                    color = currentTheme.glowColor
                )
            )

            // Floating score popup
            val comboText = if (comboStreak > 1) "COMBO x$comboStreak! +$earnedScore" else "+$earnedScore"
            floatingScores.add(
                FloatingScore(
                    id = ++scoreIdCounter,
                    text = comboText,
                    x = avgX,
                    y = avgY,
                    isCombo = comboStreak > 1
                )
            )

            haptic?.playLineBlast(comboStreak)
            if (comboStreak >= 3) {
                haptic?.playComboImpact()
            }

            // Check level completion
            if (currentMode == GameMode.LEVELS || currentMode == GameMode.DAILY_CHALLENGE) {
                if (score >= levelTargetScore && levelLinesCurrent >= levelLinesTarget) {
                    isLevelCompleted = true
                }
            }
        } else {
            comboStreak = 0
        }

        // If all 3 pieces used, spawn 3 new pieces
        if (availablePieces.all { it == null }) {
            spawnNewPieces()
        } else {
            checkGameOver()
        }

        return true
    }

    private fun spawnBlastParticles(centerX: Float, centerY: Float, color: Color, cellSize: Float) {
        val particleCount = 14
        for (i in 0 until particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * cellSize * 2.2f + (cellSize * 0.5f)
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed - (cellSize * 0.8f) // slight upward blast
            val vz = (Random.nextFloat() - 0.5f) * 15f

            particles.add(
                Particle3D(
                    x = centerX + (Random.nextFloat() - 0.5f) * cellSize * 0.4f,
                    y = centerY + (Random.nextFloat() - 0.5f) * cellSize * 0.4f,
                    z = 0f,
                    vx = vx,
                    vy = vy,
                    vz = vz,
                    size = cellSize * Random.nextFloat().coerceIn(0.18f, 0.38f),
                    color = color,
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 450f,
                    life = 1f,
                    maxLife = 0.85f + Random.nextFloat() * 0.35f
                )
            )
        }
    }

    fun updatePhysics(dt: Float) {
        val gravity = 980f // gravity in px/s^2

        // Particles update
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.vy += gravity * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt
            p.rotation += p.rotationSpeed * dt
            p.life -= dt / p.maxLife
            p.alpha = (p.life).coerceIn(0f, 1f)

            if (p.life <= 0f) {
                pIter.remove()
            }
        }

        // Shockwaves update
        val sIter = shockwaves.iterator()
        while (sIter.hasNext()) {
            val s = sIter.next()
            s.radius += (s.maxRadius * 2.8f) * dt
            s.alpha = (1f - (s.radius / s.maxRadius)).coerceIn(0f, 1f)
            if (s.radius >= s.maxRadius) {
                sIter.remove()
            }
        }

        // Floating scores update
        val fIter = floatingScores.iterator()
        while (fIter.hasNext()) {
            val f = fIter.next()
            f.y -= 75f * dt
            f.alpha -= 0.9f * dt
            f.scale = (f.scale + 0.3f * dt).coerceAtMost(1.4f)
            if (f.alpha <= 0f) {
                fIter.remove()
            }
        }
    }

    fun addGarbageBlocks(count: Int) {
        var added = 0
        val candidates = mutableListOf<CellCoord>()
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (grid[r][c] == null) {
                    candidates.add(CellCoord(r, c))
                }
            }
        }
        candidates.shuffle()
        for (coord in candidates.take(count)) {
            grid[coord.row][coord.col] = PlacedCell(colorIndex = 5, isGarbage = true)
            added++
        }
        checkGameOver()
    }

    fun tickTimer(haptic: SoundHapticEffect?) {
        if (currentMode == GameMode.TIME_ATTACK && !isGameOver) {
            remainingTimeSeconds--
            if (remainingTimeSeconds <= 0) {
                remainingTimeSeconds = 0
                isGameOver = true
                haptic?.playGameOver()
            }
        }
    }

    fun checkGameOver(): Boolean {
        val remainingPieces = availablePieces.filterNotNull()
        if (remainingPieces.isEmpty()) {
            isGameOver = false
            return false
        }

        // Check if ANY piece can fit in ANY cell
        for (piece in remainingPieces) {
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    if (canPlacePiece(piece, r, c)) {
                        isGameOver = false
                        return false
                    }
                }
            }
        }

        isGameOver = true
        return true
    }
}
