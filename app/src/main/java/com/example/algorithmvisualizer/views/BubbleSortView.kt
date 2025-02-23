package com.example.algorithmvisualizer.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.BubbleSortStep
import com.example.algorithmvisualizer.algorithm.generateBubbleSortSteps
import com.example.algorithmvisualizer.algorithm.resetArray
import com.example.algorithmvisualizer.components.ExplanationCard
import com.example.algorithmvisualizer.components.PlaybackControls
import com.example.algorithmvisualizer.components.TimelineSlider
import com.example.algorithmvisualizer.ui.theme.DarkGray
import com.example.algorithmvisualizer.ui.theme.LightGray
import com.example.algorithmvisualizer.ui.theme.Primary
import com.example.algorithmvisualizer.ui.theme.Secondary
import kotlinx.coroutines.delay

/**
 * BubbleSortView is a composable function that displays the visualization
 * of the Bubble Sort algorithm.
 *
 * This view consists of:
 * - An explanation card that describes the Bubble Sort algorithm.
 * - A canvas area where the array is drawn as vertical bars.
 * - A timeline slider to control the progress of the animation.
 * - Control buttons (play/pause, previous, next, reset) to navigate through the steps.
 */
@Composable
fun BubbleSortView() {
    val arraySize = 10
    val arrayState = remember { mutableStateListOf<Int>() }
    val steps = remember { mutableStateListOf<BubbleSortStep>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    // Initialize the array and generate the BubbleSort steps.
    LaunchedEffect(Unit) {
        resetArray(arrayState, arraySize, maxNumber = 500)
        steps.clear()
        steps.addAll(generateBubbleSortSteps(arrayState))
        currentStepIndex = 0
    }

    // Auto-play logic: advance steps if playing.
    LaunchedEffect(isPlaying, currentStepIndex) {
        if (isPlaying) {
            if (steps.isNotEmpty() && currentStepIndex < steps.lastIndex) {
                delay(200L)
                currentStepIndex++
            } else {
                isPlaying = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Explanation Card (can be refactored into a reusable component).
        ExplanationCard(
            explanationText = stringResource(id = R.string.explanation_text_bubble)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Visualization area for the Bubble Sort animation.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (steps.isNotEmpty()) {
                val currentStep = steps[currentStepIndex]
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(width = 2.dp, color = LightGray)
                ) {
                    val slotWidth = size.width / currentStep.array.size
                    val barWidth = slotWidth * 0.8f
                    val horizontalPadding = (slotWidth - barWidth) / 2f

                    val textPaint = android.graphics.Paint().apply {
                        color = DarkGray.toArgb()
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    currentStep.array.forEachIndexed { index, value ->
                        val barColor = if (
                            index == currentStep.comparedIndices.first ||
                            index == currentStep.comparedIndices.second
                        ) Secondary else Primary

                        val leftX = index * slotWidth + horizontalPadding
                        val topY = size.height - value

                        drawRect(
                            color = barColor,
                            topLeft = Offset(leftX, topY),
                            size = Size(barWidth, value.toFloat())
                        )

                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                value.toString(),
                                leftX + (barWidth / 2f),
                                topY - 10,
                                textPaint
                            )
                        }
                    }
                }
            } else {
                // While steps are loading, show a loading indicator.
                Text(
                    text = "Loading...",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render the TimelineSlider only if there are steps.
        if (steps.isNotEmpty()) {
            TimelineSlider(
                currentStep = currentStepIndex.toFloat(),
                maxStep = (steps.size - 1).toFloat(),
                onStepChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, steps.lastIndex)
                    isPlaying = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render PlaybackControls only if there are steps.
        if (steps.isNotEmpty()) {
            PlaybackControls(
                isPlaying = isPlaying,
                onPlayPauseToggle = { isPlaying = !isPlaying },
                onPrevious = {
                    currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                    isPlaying = false
                },
                onNext = {
                    currentStepIndex = (currentStepIndex + 1).coerceAtMost(steps.lastIndex)
                    isPlaying = false
                },
                onReset = {
                    isPlaying = false
                    resetArray(arrayState, arraySize, maxNumber = 500)
                    steps.clear()
                    steps.addAll(generateBubbleSortSteps(arrayState))
                    currentStepIndex = 0
                }
            )
        }
    }
}
