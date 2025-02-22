package com.example.algorithmvisualizer.algorithm

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Represents a single step in the visualization of Dijkstra's algorithm.
 *
 * @property distances A list containing the current distances from the source node to every node.
 * @property visited A list of booleans indicating whether each node has been finalized (visited).
 * @property currentNode The index of the node currently being processed (-1 if not applicable).
 * @property pathEdges A list of pairs representing the edges that form the final shortest path.
 * @property iterationCount (Optional) The number of iterations performed to find the best route.
 */
data class DijkstraGraphStep(
    val distances: List<Int>,
    val visited: List<Boolean>,
    val currentNode: Int,
    val pathEdges: List<Pair<Int, Int>> = emptyList(),
    val iterationCount: Int? = null
)

/**
 * Represents a weighted edge in a graph.
 *
 * @property target The index of the target node that this edge connects to.
 * @property weight The cost or weight associated with traversing this edge.
 */
data class WeightedEdge(
    val target: Int,
    val weight: Int
)

/**
 * Generates a sequence of steps to visualize the execution of Dijkstra's algorithm.
 *
 * This function simulates Dijkstra's algorithm on a weighted graph (represented as an adjacency list)
 * from a given source node to a destination node. It collects the state of the algorithm at each significant
 * step so that the progression can be visualized.
 *
 * Steps performed:
 * 1. Initialize all distances to infinity and mark all nodes as unvisited.
 * 2. Set the starting node's distance to 0.
 * 3. At each iteration, choose the unvisited node with the smallest distance.
 * 4. Mark the chosen node as visited and update the distances of its neighbors.
 * 5. Record the state after each update to allow step-by-step visualization.
 * 6. Once the algorithm completes, backtrack from the end node using the parent array to reconstruct
 *    the shortest path and add the final step.
 *
 * @param adjacency The weighted graph represented as an adjacency list.
 * @param start The index of the starting (source) node.
 * @param end The index of the destination node.
 * @return A list of [DijkstraGraphStep] objects representing each step of the algorithm.
 */
fun generateDijkstraSteps(
    adjacency: List<List<WeightedEdge>>,
    start: Int,
    end: Int
): List<DijkstraGraphStep> {
    // List to store each step for visualization
    val steps = mutableListOf<DijkstraGraphStep>()
    val n = adjacency.size

    // Initialize distances to infinity and visited status to false for all nodes.
    val dist = MutableList(n) { Int.MAX_VALUE }
    val visited = MutableList(n) { false }
    // Parent array for reconstructing the shortest path later.
    val parent = MutableList(n) { -1 }

    // Set the distance to the start node as 0.
    dist[start] = 0

    // Record the initial state before processing any node.
    steps.add(DijkstraGraphStep(distances = dist.toList(), visited = visited.toList(), currentNode = -1))

    var bestIteration: Int? = null

    // Main loop: iterate at most 'n' times.
    for (i in 0 until n) {
        var u = -1
        var minDist = Int.MAX_VALUE

        // Select the unvisited node with the smallest known distance.
        for (j in 0 until n) {
            if (!visited[j] && dist[j] < minDist) {
                u = j
                minDist = dist[j]
            }
        }
        // If no such node is found, the remaining nodes are unreachable; break the loop.
        if (u == -1) break

        // Mark the selected node as visited.
        visited[u] = true
        // Record iteration count when destination node is reached for the first time.
        if (u == end && bestIteration == null) {
            bestIteration = i + 1
        }
        // Record the state after marking the node as visited.
        steps.add(DijkstraGraphStep(distances = dist.toList(), visited = visited.toList(), currentNode = u))

        // Update the distances for all neighbors of the current node 'u'.
        for (edge in adjacency[u]) {
            val v = edge.target
            val w = edge.weight
            // Only update if neighbor 'v' has not been visited and 'u' is reachable.
            if (!visited[v] && dist[u] != Int.MAX_VALUE) {
                val newDist = dist[u] + w
                // If a shorter path to 'v' is found, update its distance and parent.
                if (newDist < dist[v]) {
                    dist[v] = newDist
                    parent[v] = u
                    // Record the state after updating the distance.
                    steps.add(DijkstraGraphStep(distances = dist.toList(), visited = visited.toList(), currentNode = u))
                }
            }
        }
    }

    // Backtrack from the destination node 'end' to the source node 'start'
    // using the 'parent' array to construct the list of edges in the shortest path.
    val pathEdges = mutableListOf<Pair<Int, Int>>()
    var cur = end
    while (cur != -1 && cur != start) {
        val p = parent[cur]
        if (p == -1) break // Incomplete path if no parent is found.
        // Ensure the edge is stored with the smaller index first.
        val edge = if (p < cur) Pair(p, cur) else Pair(cur, p)
        pathEdges.add(edge)
        cur = p
    }
    // Record the final state including the reconstructed path and iteration count.
    steps.add(
        DijkstraGraphStep(
            distances = dist.toList(),
            visited = visited.toList(),
            currentNode = -1,
            pathEdges = pathEdges,
            iterationCount = bestIteration
        )
    )
    return steps
}

/**
 * Constructs a weighted adjacency list for a graph based on the spatial positions of nodes.
 *
 * This function connects nodes if they are within a specified maximum distance from each other.
 * Each edge is assigned a random weight between 1 and 15. The function also distinguishes diagonal
 * edges from non-diagonal ones:
 *
 * - Non-diagonal edges: An edge is added immediately between nodes that are close enough and are not diagonal.
 * - Diagonal edges: For nodes that lie diagonally relative to each other (both x and y differences significant
 *   and the ratio of dx/dy is approximately 1), only the best (shortest) diagonal connection per node is kept.
 *
 * @param positions A list of (x, y) coordinate pairs representing node positions.
 * @param maxDistance The maximum distance threshold to consider two nodes as neighbors.
 * @return A weighted adjacency list where each index corresponds to a list of [WeightedEdge] objects.
 */
fun buildNearbyWeightedAdjacency(
    positions: List<Pair<Float, Float>>,
    maxDistance: Float = 0.3f
): List<MutableList<WeightedEdge>> {
    val n = positions.size
    // Create an adjacency list with an empty mutable list for each node.
    val adjacency = List(n) { mutableListOf<WeightedEdge>() }
    // Map to store the best diagonal edge candidate for each node.
    val bestDiagonal = mutableMapOf<Int, Pair<Int, Float>>()

    // Iterate over each unique pair of nodes.
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            val (x1, y1) = positions[i]
            val (x2, y2) = positions[j]
            val dx = x1 - x2
            val dy = y1 - y2
            // Compute the Euclidean distance between node i and node j.
            val distance = sqrt(dx * dx + dy * dy)

            // If the nodes are within the maximum allowed distance, consider adding an edge.
            if (distance <= maxDistance) {
                // Check if the edge is diagonal:
                // A diagonal edge has significant differences in both x and y and the ratio |dx/dy| is roughly 1.
                val isDiagonal = (abs(dx) > 0.001f && abs(dy) > 0.001f && abs(dx / dy) in 0.8f..1.2f)
                if (isDiagonal) {
                    // For diagonal edges, record the best (shortest) candidate for each node.
                    val currentBestI = bestDiagonal[i]
                    if (currentBestI == null || distance < currentBestI.second) {
                        bestDiagonal[i] = Pair(j, distance)
                    }
                    val currentBestJ = bestDiagonal[j]
                    if (currentBestJ == null || distance < currentBestJ.second) {
                        bestDiagonal[j] = Pair(i, distance)
                    }
                } else {
                    // For non-diagonal edges, add the edge immediately with a random weight.
                    val weight = Random.nextInt(1, 16)
                    adjacency[i].add(WeightedEdge(target = j, weight = weight))
                    adjacency[j].add(WeightedEdge(target = i, weight = weight))
                }
            }
        }
    }

    // Add the best diagonal edges from the bestDiagonal map.
    for ((i, candidate) in bestDiagonal) {
        val j = candidate.first
        // Ensure that each edge is added only once (by checking order).
        if (i < j) {
            val weight = Random.nextInt(1, 16)
            adjacency[i].add(WeightedEdge(target = j, weight = weight))
            adjacency[j].add(WeightedEdge(target = i, weight = weight))
        }
    }
    return adjacency
}
