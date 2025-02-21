package com.example.algorithmvisualizer.algorithm

import kotlin.random.Random

/**
 * Represents a single step in the visualization of the BFS algorithm.
 *
 * @property visited A list of booleans indicating whether each node has been visited.
 * @property currentNode The index of the node currently being processed (-1 if none).
 * @property visitOrder A list representing the order in which nodes were visited.
 */
data class BFSGraphStep(
    val visited: List<Boolean>,
    val currentNode: Int,
    val visitOrder: List<Int> = emptyList()
)

/**
 * Generates a random adjacency list for a graph with the given [size].
 *
 * This function guarantees connectivity by creating a random chain (spanning tree)
 * in which each node is connected to its consecutive node. Additionally, it optionally
 * connects the first and last nodes if both have less than two connections, ensuring that
 * each node has at most two connections.
 *
 * @param size Total number of nodes in the graph.
 * @return A list representing the adjacency list where each index maps to a list of connected node indices.
 */
fun buildRandomAdjacency(size: Int): List<List<Int>> {
    // Initialize the adjacency list.
    val adjacency = List(size) { mutableListOf<Int>() }
    // Create a random permutation of nodes.
    val nodes = (0 until size).toMutableList()
    nodes.shuffle()
    // Connect consecutive nodes to form a chain.
    for (i in 0 until size - 1) {
        val u = nodes[i]
        val v = nodes[i + 1]
        adjacency[u].add(v)
        adjacency[v].add(u)
    }
    // Optionally, connect the first and last nodes if both have fewer than 2 connections.
    val first = nodes.first()
    val last = nodes.last()
    if (adjacency[first].size < 2 && adjacency[last].size < 2 && Random.nextFloat() < 0.5f) {
        adjacency[first].add(last)
        adjacency[last].add(first)
    }
    return adjacency
}

/**
 * Executes the BFS algorithm on the provided [adjacency] list, starting from the node [start].
 *
 * As the algorithm progresses, each step is recorded with the current state of the visited nodes,
 * the node being processed, and the order in which nodes have been visited.
 *
 * @param adjacency The graph represented as an adjacency list.
 * @param start The starting node index for BFS.
 * @return A list of BFSGraphStep objects representing each step of the BFS traversal.
 */
fun generateBFSSteps(adjacency: List<List<Int>>, start: Int = 0): List<BFSGraphStep> {
    val steps = mutableListOf<BFSGraphStep>()
    val n = adjacency.size
    val visited = MutableList(n) { false }
    val queue = ArrayDeque<Int>()
    val orderList = mutableListOf<Int>()

    // Mark the starting node as visited and add it to the queue.
    visited[start] = true
    queue.add(start)
    orderList.add(start)
    steps.add(BFSGraphStep(visited.toList(), start, orderList.toList()))

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        // For each neighbor of the current node...
        for (neighbor in adjacency[current]) {
            // Record the state before processing the neighbor.
            steps.add(BFSGraphStep(visited.toList(), current, orderList.toList()))
            if (!visited[neighbor]) {
                // Visit the neighbor.
                visited[neighbor] = true
                queue.add(neighbor)
                orderList.add(neighbor)
                // Record the state after the neighbor is discovered.
                steps.add(BFSGraphStep(visited.toList(), neighbor, orderList.toList()))
            }
        }
    }
    // Final step: indicate that no node is currently being processed.
    steps.add(BFSGraphStep(visited.toList(), -1, orderList.toList()))
    return steps
}
