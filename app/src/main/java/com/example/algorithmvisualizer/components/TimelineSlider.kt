package com.example.algorithmvisualizer.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.algorithmvisualizer.ui.theme.LightGray
import com.example.algorithmvisualizer.ui.theme.Primary

@Composable
fun TimelineSlider(
    currentStep: Float,
    maxStep: Float,
    onStepChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = currentStep,
        onValueChange = onStepChange,
        valueRange = 0f..maxStep,
        colors = SliderDefaults.colors(
            thumbColor = Primary,
            activeTrackColor = Primary,
            inactiveTrackColor = LightGray
        ),
        modifier = modifier.fillMaxWidth()
    )
}
