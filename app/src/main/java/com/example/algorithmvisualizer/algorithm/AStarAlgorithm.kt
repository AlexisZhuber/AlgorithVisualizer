package com.example.algorithmvisualizer.algorithm

import kotlin.math.sqrt
import kotlin.math.pow

/**
 * Represents a step in the visualization of the A* algorithm.
 *
 * @property gScores List of accumulated costs from the start node.
 * @property visited List of booleans indicating whether a node has been closed.
 * @property currentNode The node that is currently being processed.
 * @property pathEdges List of pairs representing the edges of the optimal path.
 * @property iterationCount (Optional) Number of iterations performed.
 */
data class AStarGraphStep(
    val gScores: List<Int>,
    val visited: List<Boolean>,
    val currentNode: Int,
    val pathEdges: List<Pair<Int, Int>> = emptyList(),
    val iterationCount: Int? = null
)

/**
 * Heuristic function: calculates the Euclidean distance between the current node and the destination.
 *
 * @param node Index of the current node.
 * @param end Index of the destination node.
 * @param positions List of (normalized) positions for each node.
 * @return The Euclidean distance between the two nodes.
 */
fun heuristic(node: Int, end: Int, positions: List<Pair<Float, Float>>): Double {
    val (x1, y1) = positions[node]
    val (x2, y2) = positions[end]
    return sqrt((x2 - x1).toDouble().pow(2.0) + (y2 - y1).toDouble().pow(2.0))
}

/**
 * Generates the sequence of steps to visualize the execution of the A* algorithm.
 *
 * An open set is used to select the node with the smallest f = g + h.
 *
 * @param adjacency The graph represented as a list of lists of [WeightedEdge].
 * @param positions List of (normalized) positions for each node.
 * @param start Index of the start node.
 * @param end Index of the destination node.
 * @return A list of [AStarGraphStep] representing each step of the search.
 */
fun generateAStarSteps(
    adjacency: List<List<WeightedEdge>>,
    positions: List<Pair<Float, Float>>,
    start: Int,
    end: Int
): List<AStarGraphStep> {
    val steps = mutableListOf<AStarGraphStep>()
    val n = adjacency.size
    // Initialize g with infinity and f with maximum value.
    val g = MutableList(n) { Int.MAX_VALUE }
    val f = MutableList(n) { Double.MAX_VALUE }
    val visited = MutableList(n) { false }
    val parent = MutableList(n) { -1 }
    // Open set for nodes to be explored.
    val openSet = mutableSetOf<Int>()

    g[start] = 0
    f[start] = heuristic(start, end, positions)
    openSet.add(start)

    // Record the initial state.
    steps.add(AStarGraphStep(gScores = g.toList(), visited = visited.toList(), currentNode = -1))

    var bestIteration: Int? = null
    var iteration = 0

    while (openSet.isNotEmpty()) {
        // Select the node in the open set with the smallest f value.
        var current = -1
        var currentF = Double.MAX_VALUE
        for (node in openSet) {
            if (f[node] < currentF) {
                currentF = f[node]
                current = node
            }
        }
        // Record the step before expanding neighbors.
        steps.add(AStarGraphStep(gScores = g.toList(), visited = visited.toList(), currentNode = current))
        iteration++
        // If the destination is reached, finish the search.
        if (current == end) {
            bestIteration = iteration
            break
        }
        openSet.remove(current)
        visited[current] = true

        // For each neighbor of the current node.
        for (edge in adjacency[current]) {
            val neighbor = edge.target
            if (visited[neighbor]) continue
            val tentativeG = if (g[current] == Int.MAX_VALUE) Int.MAX_VALUE else g[current] + edge.weight
            if (tentativeG < g[neighbor]) {
                g[neighbor] = tentativeG
                f[neighbor] = tentativeG + heuristic(neighbor, end, positions)
                parent[neighbor] = current
                openSet.add(neighbor)
                // Record the state after updating the neighbor.
                steps.add(AStarGraphStep(gScores = g.toList(), visited = visited.toList(), currentNode = current))
            }
        }
    }

    // Reconstruct the optimal path using the parent array.
    val pathEdges = mutableListOf<Pair<Int, Int>>()
    var cur = end
    while (cur != -1 && cur != start) {
        val p = parent[cur]
        if (p == -1) break
        val edge = if (p < cur) Pair(p, cur) else Pair(cur, p)
        pathEdges.add(edge)
        cur = p
    }
    steps.add(
        AStarGraphStep(
            gScores = g.toList(),
            visited = visited.toList(),
            currentNode = -1,
            pathEdges = pathEdges,
            iterationCount = bestIteration
        )
    )
    return steps
}
