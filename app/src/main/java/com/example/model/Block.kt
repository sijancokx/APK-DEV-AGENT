package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

enum class GameMode(val displayName: String) {
    CLASSIC("Klasik"),
    TIME_ATTACK("Tantangan Waktu"),
    LEVELS("Petualangan Level"),
    DAILY_CHALLENGE("Tantangan Harian"),
    MULTIPLAYER("Duel 1v1 Daring")
}

data class CellCoord(val row: Int, val col: Int)

data class BlockPiece(
    val id: String,
    val shape: List<List<Boolean>>,
    val colorIndex: Int,
    val width: Int = shape.firstOrNull()?.size ?: 0,
    val height: Int = shape.size
) {
    val cellCount: Int = shape.sumOf { row -> row.count { it } }
}

data class PlacedCell(
    val colorIndex: Int,
    val isObstacle: Boolean = false,
    val isGarbage: Boolean = false,
    val isClearing: Boolean = false,
    val clearProgress: Float = 0f
)

data class Particle3D(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var size: Float,
    val color: Color,
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f,
    var life: Float = 1f,
    val maxLife: Float = 1f
)

data class ShockwaveRing(
    val centerX: Float,
    val centerY: Float,
    var radius: Float = 0f,
    val maxRadius: Float = 250f,
    var alpha: Float = 1f,
    val color: Color
)

data class FloatingScore(
    val id: Long,
    val text: String,
    val x: Float,
    var y: Float,
    var alpha: Float = 1f,
    var scale: Float = 1f,
    val isCombo: Boolean = false
)

enum class ThemeType(
    val title: String,
    val subtitle: String,
    val boardBackground: List<Color>,
    val gridBorder: Color,
    val cellBackground: Color,
    val palette: List<Color>,
    val highlightColor: Color,
    val shadowColor: Color,
    val glowColor: Color
) {
    CYBERPUNK(
        title = "Cyberpunk Neon",
        subtitle = "Cahaya neon futuristik & obsidian",
        boardBackground = listOf(Color(0xFF0F172A), Color(0xFF090D16)),
        gridBorder = Color(0xFF1E293B),
        cellBackground = Color(0xFF151E32),
        palette = listOf(
            Color(0xFF06B6D4), // Cyan
            Color(0xFFEC4899), // Neon Pink
            Color(0xFFF59E0B), // Amber
            Color(0xFF8B5CF6), // Purple
            Color(0xFF10B981), // Emerald
            Color(0xFFEF4444)  // Crimson
        ),
        highlightColor = Color(0x99FFFFFF),
        shadowColor = Color(0x66000000),
        glowColor = Color(0xFF00F0FF)
    ),
    CRYSTAL_GEM(
        title = "Crystal Gem",
        subtitle = "Kristal prisma berlian mewah",
        boardBackground = listOf(Color(0xFF181829), Color(0xFF0D0D17)),
        gridBorder = Color(0xFF2E2E48),
        cellBackground = Color(0xFF1C1C30),
        palette = listOf(
            Color(0xFF60A5FA), // Sapphire
            Color(0xFFF472B6), // Rose Diamond
            Color(0xFF34D399), // Emerald
            Color(0xFFA78BFA), // Amethyst
            Color(0xFFFBBF24), // Topaz
            Color(0xFF38BDF8)  // Aquamarine
        ),
        highlightColor = Color(0xB3FFFFFF),
        shadowColor = Color(0x77050510),
        glowColor = Color(0xFFE0E7FF)
    ),
    WOODCRAFT(
        title = "Woodcraft Retro",
        subtitle = "Kayu mahoni & tekstur hangat klasik",
        boardBackground = listOf(Color(0xFF2E1C14), Color(0xFF1A0E08)),
        gridBorder = Color(0xFF4A2F22),
        cellBackground = Color(0xFF382318),
        palette = listOf(
            Color(0xFFD97706), // Amber Wood
            Color(0xFFB45309), // Bronze
            Color(0xFF92400E), // Warm Walnut
            Color(0xFFCA8A04), // Brass
            Color(0xFF854D0E), // Oak
            Color(0xFFEA580C)  // Red Cedar
        ),
        highlightColor = Color(0x66FFDEB3),
        shadowColor = Color(0x88100502),
        glowColor = Color(0xFFFFB74D)
    ),
    COSMIC_GALAXY(
        title = "Cosmic Galaxy",
        subtitle = "Misteri luar angkasa & nebula kosmik",
        boardBackground = listOf(Color(0xFF13092B), Color(0xFF080314)),
        gridBorder = Color(0xFF291B4C),
        cellBackground = Color(0xFF1B0F38),
        palette = listOf(
            Color(0xFF818CF8), // Nebula Blue
            Color(0xFFC084FC), // Cosmic Violet
            Color(0xFFF43F5E), // Supernova Red
            Color(0xFF2DD4BF), // Aurora Green
            Color(0xFFFACC15), // Star Gold
            Color(0xFFE879F9)  // Starlight Pink
        ),
        highlightColor = Color(0x80FFFFFF),
        shadowColor = Color(0x99030008),
        glowColor = Color(0xFFD8B4FE)
    ),
    MAGMA_VOLCANIC(
        title = "Magma Volcanic",
        subtitle = "Batu lava pijar membara",
        boardBackground = listOf(Color(0xFF220A0A), Color(0xFF120303)),
        gridBorder = Color(0xFF441818),
        cellBackground = Color(0xFF2C1010),
        palette = listOf(
            Color(0xFFEF4444), // Lava Flame
            Color(0xFFF97316), // Molten Orange
            Color(0xFFFBBF24), // Incandescent Yellow
            Color(0xFFDC2626), // Deep Crimson
            Color(0xFFFB923C), // Ember Glow
            Color(0xFFEA580C)  // Magma Core
        ),
        highlightColor = Color(0x99FFF176),
        shadowColor = Color(0x990A0000),
        glowColor = Color(0xFFFF5722)
    )
}
