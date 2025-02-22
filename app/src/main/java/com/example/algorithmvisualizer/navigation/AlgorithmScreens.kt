package com.example.algorithmvisualizer.navigation

import androidx.compose.runtime.Composable
import com.example.algorithmvisualizer.views.AStarView
import com.example.algorithmvisualizer.views.BFSView
import com.example.algorithmvisualizer.views.BubbleSortView
import com.example.algorithmvisualizer.views.DijkstraView
import com.example.algorithmvisualizer.views.SelectionSortView

/**
 * BubbleSortScreen is a Composable function that displays the Bubble Sort visualization.
 *
 * This function acts as a navigation target for the Bubble Sort algorithm view.
 */
@Composable
fun BubbleSortScreen() {
    // Render the BubbleSortView Composable.
    BubbleSortView()
}

/**
 * SelectionSortScreen is a Composable function that displays the Selection Sort visualization.
 *
 * This function serves as a navigation target for the Selection Sort algorithm view.
 */
@Composable
fun SelectionSortScreen() {
    // Render the SelectionSortView Composable.
    SelectionSortView()
}

/**
 * BFSScreen is a Composable function that displays the Breadth-First Search (BFS) visualization.
 *
 * This function serves as a navigation target for the BFS algorithm view.
 */
@Composable
fun BFSScreen() {
    // Render the BFSView Composable.
    BFSView()
}

/**
 * DijkstraScreen is a Composable function that displays the Dijkstra's algorithm visualization.
 *
 * This function acts as a navigation target for the Dijkstra algorithm view.
 */
@Composable
fun DijkstraScreen() {
    // Render the DijkstraView Composable.
    DijkstraView()
}

/**
 * AStarScreen is a Composable function that displays the A* algorithm visualization.
 *
 * This function acts as a navigation target for the A* algorithm view.
 */
@Composable
fun AStarScreen() {
    // Render the AStarView Composable.
    AStarView()
}
