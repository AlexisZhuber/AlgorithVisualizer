package com.example.algorithmvisualizer.util

import kotlin.math.cos
import kotlin.math.sin

/**
 * Generates a layout for nodes using two concentric circles.
 *
 * The nodes are divided into two groups:
 * - The outer circle uses a radius of [outerRadius].
 * - The inner circle uses a radius of [innerRadius].
 *
 * The resulting positions are normalized (i.e., within the range [0,1]) and centered at (0.5, 0.5).
 *
 * @param size The total number of nodes.
 * @param outerRadius The radius for the outer circle (e.g., 0.45).
 * @param innerRadius The radius for the inner circle (e.g., 0.25).
 * @return A list of node positions represented as Pair<Float, Float>.
 */
fun buildConcentricCircleLayout(
    size: Int,
    outerRadius: Float = 0.45f,
    innerRadius: Float = 0.25f
): List<Pair<Float, Float>> {
    val positions = mutableListOf<Pair<Float, Float>>()

    // Determine the number of nodes to assign to the outer circle.
    val outerCount = if (size % 2 == 0) size / 2 else size / 2 + 1
    val innerCount = size - outerCount

    // Fixed center at (0.5, 0.5)
    val centerX = 0.5f
    val centerY = 0.5f

    // Generate positions for the outer circle using uniformly spaced angles.
    for (i in 0 until outerCount) {
        val angle = 2 * Math.PI * i / outerCount
        val x = centerX + outerRadius * cos(angle).toFloat()
        val y = centerY + outerRadius * sin(angle).toFloat()
        positions.add(x to y)
    }

    // Generate positions for the inner circle.
    // Angles are uniformly spaced with an additional offset for better distribution.
    for (i in 0 until innerCount) {
        val angle = 2 * Math.PI * i / innerCount + Math.PI / innerCount
        val x = centerX + innerRadius * cos(angle).toFloat()
        val y = centerY + innerRadius * sin(angle).toFloat()
        positions.add(x to y)
    }

    return positions
}
