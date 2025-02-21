package com.example.algorithmvisualizer.model

sealed class Screen(val route: String) {
    object BubbleSort : Screen("bubble_sort")
    object SelectionSort : Screen("selection_sort")
    object BFS : Screen("BFS")
    object Dijkstra : Screen("dijkstra")
    object QuickSort : Screen("quick_sort")
}
