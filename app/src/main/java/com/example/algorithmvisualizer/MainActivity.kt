package com.example.algorithmvisualizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.algorithmvisualizer.ui.theme.AlgorithmVisualizerTheme
import com.example.algorithmvisualizer.views.BubbleSortView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlgorithmVisualizerTheme {
                BubbleSortView()
            }
        }
    }
}

