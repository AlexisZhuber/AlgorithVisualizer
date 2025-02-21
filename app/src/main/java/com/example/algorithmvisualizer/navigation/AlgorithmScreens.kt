package com.example.algorithmvisualizer.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.algorithmvisualizer.views.BFSView
import com.example.algorithmvisualizer.views.BubbleSortView
import com.example.algorithmvisualizer.views.DijkstraView
import com.example.algorithmvisualizer.views.SelectionSortView

@Composable
fun BubbleSortScreen() {
    BubbleSortView()
}

@Composable
fun SelectionSortScreen() {
    SelectionSortView()
}

@Composable
fun BFSScreen() {
    BFSView()
}

@Composable
fun DijkstraScreen() {
    DijkstraView()
}

@Composable
fun QuickSortScreen() {
    Text(text = "Quick Sort Visualization")
}
