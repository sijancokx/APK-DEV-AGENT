package com.example

import com.example.engine.BlockBlastEngine
import com.example.model.BlockPiece
import com.example.model.GameMode
import com.example.model.PlacedCell
import com.example.model.ThemeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockBlastEngineTest {

    private lateinit var engine: BlockBlastEngine

    @Before
    fun setUp() {
        engine = BlockBlastEngine()
    }

    @Test
    fun testStartClassicGame() {
        engine.startNewGame(GameMode.CLASSIC, initialHighScore = 500L)
        assertEquals(GameMode.CLASSIC, engine.currentMode)
        assertEquals(0L, engine.score)
        assertEquals(500L, engine.highScore)
        assertEquals(3, engine.availablePieces.size)
        assertFalse(engine.isGameOver)
    }

    @Test
    fun testCanPlacePieceValidation() {
        engine.startNewGame(GameMode.CLASSIC)
        val singleDot = BlockPiece(
            id = "dot",
            shape = listOf(listOf(true)),
            colorIndex = 0
        )

        // Valid on empty board
        assertTrue(engine.canPlacePiece(singleDot, 0, 0))
        assertTrue(engine.canPlacePiece(singleDot, 7, 7))

        // Out of bounds
        assertFalse(engine.canPlacePiece(singleDot, 8, 0))
        assertFalse(engine.canPlacePiece(singleDot, 0, 8))
        assertFalse(engine.canPlacePiece(singleDot, -1, 0))

        // Already occupied cell
        engine.grid[3][3] = PlacedCell(colorIndex = 1)
        assertFalse(engine.canPlacePiece(singleDot, 3, 3))
    }

    @Test
    fun testLineClearingAndCombo() {
        engine.startNewGame(GameMode.CLASSIC)
        // Fill 7 cells of row 0
        for (c in 0 until 7) {
            engine.grid[0][c] = PlacedCell(colorIndex = 0)
        }

        val dotPiece = BlockPiece(
            id = "dot",
            shape = listOf(listOf(true)),
            colorIndex = 2
        )
        engine.availablePieces = mutableListOf(dotPiece, null, null)

        val placed = engine.placePiece(
            pieceIndex = 0,
            startRow = 0,
            startCol = 7,
            boardWidthPx = 400f,
            boardHeightPx = 400f,
            currentTheme = ThemeType.CYBERPUNK,
            haptic = null
        )

        assertTrue(placed)
        // Row 0 should be cleared
        for (c in 0 until 8) {
            assertEquals(null, engine.grid[0][c])
        }
        assertEquals(1, engine.comboStreak)
        assertEquals(1L, engine.linesClearedTotal)
        assertTrue(engine.score > 100)
        assertTrue(engine.particles.isNotEmpty())
        assertTrue(engine.shockwaves.isNotEmpty())
        assertTrue(engine.floatingScores.isNotEmpty())
    }

    @Test
    fun testTimeAttackTimer() {
        engine.startNewGame(GameMode.TIME_ATTACK)
        assertEquals(90, engine.remainingTimeSeconds)

        engine.tickTimer(null)
        assertEquals(89, engine.remainingTimeSeconds)
    }

    @Test
    fun testLevelProgressionSetup() {
        engine.startNewGame(GameMode.LEVELS, level = 3)
        assertEquals(3, engine.currentLevel)
        assertTrue(engine.levelTargetScore > 1000L)
        // Check obstacle blocks placed
        var obstaclesFound = 0
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                if (engine.grid[r][c]?.isObstacle == true) {
                    obstaclesFound++
                }
            }
        }
        assertTrue(obstaclesFound > 0)
    }
}
