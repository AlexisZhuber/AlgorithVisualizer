package com.example.algorithmvisualizer.util

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

/**
 * Returns a list of (x, y) positions arranged in a circle.
 * [centerX], [centerY] is the circle center; [radius] is how big the circle is.
 */
fun buildCircleLayout(
    nodeCount: Int,
    centerX: Float,
    centerY: Float,
    radius: Float
): List<Offset> {
    val positions = mutableListOf<Offset>()
    val angleStep = (2 * Math.PI / nodeCount)

    for (i in 0 until nodeCount) {
        val angle = i * angleStep
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        positions.add(Offset(x, y))
    }
    return positions
}