package com.example.ui.game

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.engine.BlockBlastEngine
import com.example.model.BlockPiece
import com.example.model.PlacedCell
import com.example.model.ThemeType

@Composable
fun BoardCanvas3D(
    engine: BlockBlastEngine,
    theme: ThemeType,
    hoveredPiece: BlockPiece?,
    hoverCoord: Pair<Int, Int>?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size.width
            val gridSize = engine.gridSize
            val cellSize = boardSize / gridSize
            val cellPadding = cellSize * 0.08f
            val blockSize = cellSize - cellPadding * 2

            // 1. Draw Board Background with subtle depth gradient
            drawRoundRect(
                brush = Brush.verticalGradient(theme.boardBackground),
                size = size,
                cornerRadius = CornerRadius(24f, 24f)
            )

            // Board outer border
            drawRoundRect(
                color = theme.gridBorder,
                size = size,
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 6f)
            )

            // 2. Draw 8x8 Inset Grid Cells
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val x = c * cellSize + cellPadding
                    val y = r * cellSize + cellPadding
                    val cell = engine.grid[r][c]

                    // Draw recessed slot background
                    drawRecessedSlot(
                        x = x,
                        y = y,
                        size = blockSize,
                        slotColor = theme.cellBackground,
                        borderColor = theme.gridBorder
                    )

                    // Draw placed 3D block
                    if (cell != null) {
                        draw3DBlock(
                            x = x,
                            y = y,
                            size = blockSize,
                            cell = cell,
                            theme = theme
                        )
                    }
                }
            }

            // 3. Draw Phantom Ghost Hover Preview
            if (hoveredPiece != null && hoverCoord != null) {
                val (hoverR, hoverC) = hoverCoord
                val canPlace = engine.canPlacePiece(hoveredPiece, hoverR, hoverC)
                if (canPlace) {
                    for (r in 0 until hoveredPiece.height) {
                        for (c in 0 until hoveredPiece.width) {
                            if (hoveredPiece.shape[r][c]) {
                                val targetR = hoverR + r
                                val targetC = hoverC + c
                                if (targetR in 0 until gridSize && targetC in 0 until gridSize) {
                                    val x = targetC * cellSize + cellPadding
                                    val y = targetR * cellSize + cellPadding
                                    val previewColor = theme.palette[hoveredPiece.colorIndex % theme.palette.size]
                                    drawGhostPreviewBlock(
                                        x = x,
                                        y = y,
                                        size = blockSize,
                                        color = previewColor,
                                        glowColor = theme.glowColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Draw Expanding Shockwave Rings
            for (shock in engine.shockwaves) {
                drawCircle(
                    color = shock.color.copy(alpha = shock.alpha * 0.7f),
                    radius = shock.radius,
                    center = Offset(shock.centerX, shock.centerY),
                    style = Stroke(width = 8f * shock.alpha)
                )
            }

            // 5. Draw 3D Tumbling Particles
            for (p in engine.particles) {
                rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                    val half = p.size / 2f
                    // Draw shard face
                    drawRect(
                        color = p.color.copy(alpha = p.alpha),
                        topLeft = Offset(p.x - half, p.y - half),
                        size = Size(p.size, p.size)
                    )
                    // Draw 3D shard highlight
                    drawRect(
                        color = Color.White.copy(alpha = p.alpha * 0.6f),
                        topLeft = Offset(p.x - half, p.y - half),
                        size = Size(p.size * 0.4f, p.size * 0.4f)
                    )
                    // Draw 3D shard shadow
                    drawRect(
                        color = Color.Black.copy(alpha = p.alpha * 0.4f),
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.size * 0.5f, p.size * 0.5f)
                    )
                }
            }

            // 6. Draw Floating Score Popups
            drawContext.canvas.nativeCanvas.apply {
                for (f in engine.floatingScores) {
                    val paint = Paint().apply {
                        color = if (f.isCombo) 0xFFFFD700.toInt() else 0xFFFFFFFF.toInt()
                        textSize = if (f.isCombo) 48f * f.scale else 36f * f.scale
                        isFakeBoldText = true
                        alpha = (f.alpha * 255).toInt().coerceIn(0, 255)
                        textAlign = Paint.Align.CENTER
                        setShadowLayer(16f, 0f, 4f, 0xCC000000.toInt())
                    }
                    drawText(f.text, f.x, f.y, paint)
                }
            }
        }
    }
}

// Inset recessed slot for empty grid position
private fun DrawScope.drawRecessedSlot(
    x: Float,
    y: Float,
    size: Float,
    slotColor: Color,
    borderColor: Color
) {
    val corner = CornerRadius(14f, 14f)

    // Dark inset shadow
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = corner
    )

    // Base slot cavity
    drawRoundRect(
        color = slotColor,
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(size - 4f, size - 4f),
        cornerRadius = corner
    )

    // Subtle inner groove line
    drawRoundRect(
        color = borderColor.copy(alpha = 0.5f),
        topLeft = Offset(x + 1f, y + 1f),
        size = Size(size - 2f, size - 2f),
        cornerRadius = corner,
        style = Stroke(width = 2f)
    )
}

// Full 3D beveled block with highlights, shadows, and gloss
private fun DrawScope.draw3DBlock(
    x: Float,
    y: Float,
    size: Float,
    cell: PlacedCell,
    theme: ThemeType
) {
    val baseColor = when {
        cell.isObstacle -> Color(0xFF64748B) // Slate Stone
        cell.isGarbage -> Color(0xFFDC2626)  // Garbage Red Alert
        cell.colorIndex in theme.palette.indices -> theme.palette[cell.colorIndex]
        else -> theme.palette.first()
    }

    val corner = CornerRadius(14f, 14f)
    val bevel = size * 0.16f

    // 1. Drop shadow beneath the block
    drawRoundRect(
        color = Color(0x88000000),
        topLeft = Offset(x + 3f, y + 5f),
        size = Size(size - 2f, size - 2f),
        cornerRadius = corner
    )

    // 2. Main base block body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                baseColor,
                baseColor.copy(red = baseColor.red * 0.75f, green = baseColor.green * 0.75f, blue = baseColor.blue * 0.75f)
            ),
            startY = y,
            endY = y + size
        ),
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = corner
    )

    // 3. Top-left bevel highlight polygon (simulating 3D light from top-left)
    val highlightPath = Path().apply {
        moveTo(x, y)
        lineTo(x + size, y)
        lineTo(x + size - bevel, y + bevel)
        lineTo(x + bevel, y + bevel)
        lineTo(x + bevel, y + size - bevel)
        lineTo(x, y + size)
        close()
    }
    drawPath(
        path = highlightPath,
        color = theme.highlightColor.copy(alpha = 0.45f),
        style = Fill
    )

    // 4. Bottom-right bevel shadow polygon
    val shadowPath = Path().apply {
        moveTo(x + size, y)
        lineTo(x + size, y + size)
        lineTo(x, y + size)
        lineTo(x + bevel, y + size - bevel)
        lineTo(x + size - bevel, y + size - bevel)
        lineTo(x + size - bevel, y + bevel)
        close()
    }
    drawPath(
        path = shadowPath,
        color = theme.shadowColor.copy(alpha = 0.55f),
        style = Fill
    )

    // 5. Center face
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.95f),
                baseColor.copy(red = baseColor.red * 0.85f, green = baseColor.green * 0.85f, blue = baseColor.blue * 0.85f)
            ),
            center = Offset(x + size * 0.4f, y + size * 0.4f),
            radius = size * 0.6f
        ),
        topLeft = Offset(x + bevel, y + bevel),
        size = Size(size - bevel * 2, size - bevel * 2),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // 6. Specular glossy reflection pill (3D glassy finish)
    drawRoundRect(
        color = Color.White.copy(alpha = 0.4f),
        topLeft = Offset(x + bevel + 3f, y + bevel + 3f),
        size = Size(size * 0.35f, size * 0.18f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // 7. Extra texture detail for obstacles/garbage
    if (cell.isObstacle) {
        // Frozen ice/stone crack cross
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(x + size * 0.3f, y + size * 0.3f),
            end = Offset(x + size * 0.7f, y + size * 0.7f),
            strokeWidth = 3f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(x + size * 0.7f, y + size * 0.3f),
            end = Offset(x + size * 0.3f, y + size * 0.7f),
            strokeWidth = 3f
        )
    }
}

// Glowing ghost preview block when dragging over valid drop zone
private fun DrawScope.drawGhostPreviewBlock(
    x: Float,
    y: Float,
    size: Float,
    color: Color,
    glowColor: Color
) {
    val corner = CornerRadius(14f, 14f)

    // Outer glow aura
    drawRoundRect(
        color = glowColor.copy(alpha = 0.35f),
        topLeft = Offset(x - 2f, y - 2f),
        size = Size(size + 4f, size + 4f),
        cornerRadius = corner
    )

    // Semi-transparent phantom body
    drawRoundRect(
        color = color.copy(alpha = 0.55f),
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = corner
    )

    // Crisp neon outline
    drawRoundRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = corner,
        style = Stroke(width = 3.5f)
    )
}
