package com.kotobyte.utils.vector

sealed class VectorPathCommand {

    data class Move(
            val x: Float,
            val y: Float,
            val isRelative: Boolean

    ) : VectorPathCommand()

    data class Cubic(
            val x1: Float,
            val y1: Float,
            val x2: Float,
            val y2: Float,
            val x3: Float,
            val y3: Float,
            val isRelative: Boolean

    ) : VectorPathCommand()
}
