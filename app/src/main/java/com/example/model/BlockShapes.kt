package com.example.model

import java.util.UUID
import kotlin.random.Random

object BlockShapes {
    // 1x1 Single
    private val SHAPE_DOT = listOf(listOf(true))

    // 1x2 and 2x1 Domino
    private val SHAPE_LINE_2_H = listOf(listOf(true, true))
    private val SHAPE_LINE_2_V = listOf(listOf(true), listOf(true))

    // 1x3 and 3x1 Tromino
    private val SHAPE_LINE_3_H = listOf(listOf(true, true, true))
    private val SHAPE_LINE_3_V = listOf(listOf(true), listOf(true), listOf(true))

    // 1x4 and 4x1 Tetromino
    private val SHAPE_LINE_4_H = listOf(listOf(true, true, true, true))
    private val SHAPE_LINE_4_V = listOf(listOf(true), listOf(true), listOf(true), listOf(true))

    // 1x5 and 5x1 Pentomino
    private val SHAPE_LINE_5_H = listOf(listOf(true, true, true, true, true))
    private val SHAPE_LINE_5_V = listOf(listOf(true), listOf(true), listOf(true), listOf(true), listOf(true))

    // 2x2 Square
    private val SHAPE_SQUARE_2 = listOf(
        listOf(true, true),
        listOf(true, true)
    )

    // 3x3 Square (rare big block)
    private val SHAPE_SQUARE_3 = listOf(
        listOf(true, true, true),
        listOf(true, true, true),
        listOf(true, true, true)
    )

    // Small Corner (3 blocks)
    private val SHAPE_CORNER_TOP_LEFT = listOf(
        listOf(true, true),
        listOf(true, false)
    )
    private val SHAPE_CORNER_TOP_RIGHT = listOf(
        listOf(true, true),
        listOf(false, true)
    )
    private val SHAPE_CORNER_BOTTOM_LEFT = listOf(
        listOf(true, false),
        listOf(true, true)
    )
    private val SHAPE_CORNER_BOTTOM_RIGHT = listOf(
        listOf(false, true),
        listOf(true, true)
    )

    // Big Corner (5 blocks 3x3)
    private val SHAPE_BIG_CORNER_TL = listOf(
        listOf(true, true, true),
        listOf(true, false, false),
        listOf(true, false, false)
    )
    private val SHAPE_BIG_CORNER_BR = listOf(
        listOf(false, false, true),
        listOf(false, false, true),
        listOf(true, true, true)
    )

    // L-Shapes (4 blocks)
    private val SHAPE_L_1 = listOf(
        listOf(true, false),
        listOf(true, false),
        listOf(true, true)
    )
    private val SHAPE_L_2 = listOf(
        listOf(false, true),
        listOf(false, true),
        listOf(true, true)
    )
    private val SHAPE_L_3 = listOf(
        listOf(true, true, true),
        listOf(true, false, false)
    )
    private val SHAPE_L_4 = listOf(
        listOf(true, true, true),
        listOf(false, false, true)
    )

    // T-Shapes (4 blocks)
    private val SHAPE_T_UP = listOf(
        listOf(false, true, false),
        listOf(true, true, true)
    )
    private val SHAPE_T_DOWN = listOf(
        listOf(true, true, true),
        listOf(false, true, false)
    )
    private val SHAPE_T_LEFT = listOf(
        listOf(true, false),
        listOf(true, true),
        listOf(true, false)
    )
    private val SHAPE_T_RIGHT = listOf(
        listOf(false, true),
        listOf(true, true),
        listOf(false, true)
    )

    // Z and S Shapes
    private val SHAPE_Z_H = listOf(
        listOf(true, true, false),
        listOf(false, true, true)
    )
    private val SHAPE_S_H = listOf(
        listOf(false, true, true),
        listOf(true, true, false)
    )
    private val SHAPE_Z_V = listOf(
        listOf(false, true),
        listOf(true, true),
        listOf(true, false)
    )
    private val SHAPE_S_V = listOf(
        listOf(true, false),
        listOf(true, true),
        listOf(false, true)
    )

    // Plus shape
    private val SHAPE_PLUS = listOf(
        listOf(false, true, false),
        listOf(true, true, true),
        listOf(false, true, false)
    )

    private val ALL_SHAPES = listOf(
        SHAPE_DOT,
        SHAPE_LINE_2_H, SHAPE_LINE_2_V,
        SHAPE_LINE_3_H, SHAPE_LINE_3_V,
        SHAPE_LINE_4_H, SHAPE_LINE_4_V,
        SHAPE_LINE_5_H, SHAPE_LINE_5_V,
        SHAPE_SQUARE_2, SHAPE_SQUARE_3,
        SHAPE_CORNER_TOP_LEFT, SHAPE_CORNER_TOP_RIGHT, SHAPE_CORNER_BOTTOM_LEFT, SHAPE_CORNER_BOTTOM_RIGHT,
        SHAPE_BIG_CORNER_TL, SHAPE_BIG_CORNER_BR,
        SHAPE_L_1, SHAPE_L_2, SHAPE_L_3, SHAPE_L_4,
        SHAPE_T_UP, SHAPE_T_DOWN, SHAPE_T_LEFT, SHAPE_T_RIGHT,
        SHAPE_Z_H, SHAPE_S_H, SHAPE_Z_V, SHAPE_S_V,
        SHAPE_PLUS
    )

    fun getRandomPieces(count: Int = 3, paletteSize: Int = 6): List<BlockPiece> {
        return (0 until count).map {
            val shape = ALL_SHAPES.random()
            val colorIndex = Random.nextInt(paletteSize)
            BlockPiece(
                id = UUID.randomUUID().toString(),
                shape = shape,
                colorIndex = colorIndex
            )
        }
    }
}
