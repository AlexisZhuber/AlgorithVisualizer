package com.example.algorithmvisualizer.algorithm

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

/**
 * Holds a snapshot of BFS state at a given step.
 *
 * @param visited A list of booleans indicating whether each node is visited.
 * @param currentNode The node currently being processed in this step.
 */
data class BFSGraphStep(
    val visited: List<Boolean>,
    val currentNode: Int
)

/**
 * Builds a small fixed adjacency list of size [size].
 * For simplicity, this example connects nodes in some pattern (e.g., a small mesh).
 * Replace with your own adjacency if needed.
 */
fun buildFixedAdjacency(size: Int): List<List<Int>> {
    // Example: Hard-code edges for 5 nodes, or build a small mesh for 10, etc.
    // This example: a ring + some cross edges
    val adjacency = List(size) { mutableListOf<Int>() }

    // Simple ring
    for (i in 0 until size) {
        val next = (i + 1) % size
        adjacency[i].add(next)
        adjacency[next].add(i)
    }

    // Optionally add some random cross edges or other patterns:
    // adjacency[0].add(3)
    // adjacency[3].add(0)
    // etc.

    return adjacency
}

/**
 * Performs BFS and records each step (visited array + current node).
 * Start from [start].
 */
fun generateBFSSteps(adjacency: List<List<Int>>, start: Int = 0): List<BFSGraphStep> {
    val steps = mutableListOf<BFSGraphStep>()

    val n = adjacency.size
    val visited = MutableList(n) { false }
    val queue = ArrayDeque<Int>()

    visited[start] = true
    queue.add(start)

    // Record initial state
    steps.add(BFSGraphStep(visited.toList(), start))

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()

        // For each neighbor of current, if not visited, visit and record a step
        for (neighbor in adjacency[current]) {
            // Record the step before we process neighbor
            steps.add(BFSGraphStep(visited.toList(), current))

            if (!visited[neighbor]) {
                visited[neighbor] = true
                queue.add(neighbor)

                // Record the step after discovering neighbor
                steps.add(BFSGraphStep(visited.toList(), neighbor))
            }
        }
    }

    // Final step: no current node
    steps.add(BFSGraphStep(visited.toList(), -1))
    return steps
}

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