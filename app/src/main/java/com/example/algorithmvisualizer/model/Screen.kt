package com.example.algorithmvisualizer.model

sealed class Screen(val route: String) {
    object BubbleSort : Screen("bubble_sort")
    object SelectionSort : Screen("selection_sort")
    object BFS : Screen("BFS")
    object MergeSort : Screen("merge_sort")
    object QuickSort : Screen("quick_sort")
}
