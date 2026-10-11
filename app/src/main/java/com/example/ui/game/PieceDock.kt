package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.model.BlockPiece
import com.example.model.ThemeType
import kotlin.math.roundToInt

@Composable
fun PieceDock(
    pieces: List<BlockPiece?>,
    theme: ThemeType,
    boardBoundsInRoot: Rect?,
    onHoverChange: (BlockPiece?, Pair<Int, Int>?) -> Unit,
    onPlacePiece: (Int, Int, Int) -> Boolean,
    onPickup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        color = Color(0x33000000),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            pieces.forEachIndexed { index, piece ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(128.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (piece != null) {
                        DraggablePieceItem(
                            piece = piece,
                            pieceIndex = index,
                            theme = theme,
                            boardBoundsInRoot = boardBoundsInRoot,
                            onHoverChange = onHoverChange,
                            onPlacePiece = onPlacePiece,
                            onPickup = onPickup
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggablePieceItem(
    piece: BlockPiece,
    pieceIndex: Int,
    theme: ThemeType,
    boardBoundsInRoot: Rect?,
    onHoverChange: (BlockPiece?, Pair<Int, Int>?) -> Unit,
    onPlacePiece: (Int, Int, Int) -> Boolean,
    onPickup: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var itemBoundsInRoot by remember { mutableStateOf<Rect?>(null) }

    val density = LocalDensity.current
    val liftOffsetPx = with(density) { 70.dp.toPx() } // Lift above finger for visibility

    Box(
        modifier = Modifier
            .zIndex(if (isDragging) 99f else 1f)
            .offset {
                if (isDragging) {
                    IntOffset(dragOffsetX.roundToInt(), (dragOffsetY - liftOffsetPx).roundToInt())
                } else {
                    IntOffset.Zero
                }
            }
            .scale(if (isDragging) 1.15f else 0.85f)
            .onGloballyPositioned { coordinates ->
                itemBoundsInRoot = coordinates.boundsInRoot()
            }
            .pointerInput(piece.id) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onPickup()
                    },
                    onDragEnd = {
                        isDragging = false
                        // Check if dropped within board
                        val bounds = boardBoundsInRoot
                        val origin = itemBoundsInRoot
                        if (bounds != null && origin != null) {
                            val currentCenterX = origin.center.x + dragOffsetX
                            val currentCenterY = origin.center.y + dragOffsetY - liftOffsetPx

                            if (bounds.contains(Offset(currentCenterX, currentCenterY))) {
                                val cellSize = bounds.width / 8f
                                val relX = currentCenterX - bounds.left
                                val relY = currentCenterY - bounds.top

                                // Compute row and col (centering the piece on finger)
                                val col = ((relX / cellSize) - (piece.width / 2f)).roundToInt()
                                val row = ((relY / cellSize) - (piece.height / 2f)).roundToInt()

                                onPlacePiece(pieceIndex, row, col)
                            }
                        }
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onHoverChange(null, null)
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffsetX = 0f
                        dragOffsetY = 0f
                        onHoverChange(null, null)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y

                        // Compute hover coord
                        val bounds = boardBoundsInRoot
                        val origin = itemBoundsInRoot
                        if (bounds != null && origin != null) {
                            val currentCenterX = origin.center.x + dragOffsetX
                            val currentCenterY = origin.center.y + dragOffsetY - liftOffsetPx

                            if (bounds.contains(Offset(currentCenterX, currentCenterY))) {
                                val cellSize = bounds.width / 8f
                                val relX = currentCenterX - bounds.left
                                val relY = currentCenterY - bounds.top

                                val col = ((relX / cellSize) - (piece.width / 2f)).roundToInt()
                                val row = ((relY / cellSize) - (piece.height / 2f)).roundToInt()
                                onHoverChange(piece, Pair(row, col))
                            } else {
                                onHoverChange(null, null)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Render 3D Piece Miniature
        PieceMiniatureCanvas(
            piece = piece,
            theme = theme,
            isElevated = isDragging
        )
    }
}

@Composable
fun PieceMiniatureCanvas(
    piece: BlockPiece,
    theme: ThemeType,
    isElevated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cellUnitDp = 22.dp
    val widthDp = cellUnitDp * piece.width
    val heightDp = cellUnitDp * piece.height

    Box(
        modifier = modifier.size(widthDp, heightDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellPx = size.width / piece.width
            val pad = cellPx * 0.08f
            val blockSize = cellPx - pad * 2
            val color = theme.palette[piece.colorIndex % theme.palette.size]

            for (r in 0 until piece.height) {
                for (c in 0 until piece.width) {
                    if (piece.shape[r][c]) {
                        val bx = c * cellPx + pad
                        val by = r * cellPx + pad

                        // Shadow if elevated
                        if (isElevated) {
                            drawRoundRect(
                                color = Color(0x66000000),
                                topLeft = Offset(bx + 4f, by + 8f),
                                size = Size(blockSize, blockSize),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }

                        // Base body
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(color, color.copy(red = color.red * 0.75f, green = color.green * 0.75f, blue = color.blue * 0.75f)),
                                startY = by,
                                endY = by + blockSize
                            ),
                            topLeft = Offset(bx, by),
                            size = Size(blockSize, blockSize),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // 3D Bevel highlight
                        val bevel = blockSize * 0.18f
                        val highlightPath = Path().apply {
                            moveTo(bx, by)
                            lineTo(bx + blockSize, by)
                            lineTo(bx + blockSize - bevel, by + bevel)
                            lineTo(bx + bevel, by + bevel)
                            lineTo(bx + bevel, by + blockSize - bevel)
                            lineTo(bx, by + blockSize)
                            close()
                        }
                        drawPath(highlightPath, color = theme.highlightColor.copy(alpha = 0.5f), style = Fill)

                        // Specular pill
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.45f),
                            topLeft = Offset(bx + bevel + 2f, by + bevel + 2f),
                            size = Size(blockSize * 0.35f, blockSize * 0.2f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }
        }
    }
}
