package com.example.algorithmvisualizer.model

sealed class Screen(val route: String) {
    object BubbleSort : Screen("bubble_sort")
    object SelectionSort : Screen("selection_sort")
    object InsertionSort : Screen("insertion_sort")
    object MergeSort : Screen("merge_sort")
    object QuickSort : Screen("quick_sort")
}
