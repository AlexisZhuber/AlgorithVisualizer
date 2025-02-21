package com.example.algorithmvisualizer.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.BFSGraphStep
import com.example.algorithmvisualizer.algorithm.buildCircleLayout
import com.example.algorithmvisualizer.algorithm.buildFixedAdjacency
import com.example.algorithmvisualizer.algorithm.generateBFSSteps
import com.example.algorithmvisualizer.ui.theme.DarkGray
import com.example.algorithmvisualizer.ui.theme.LightGray
import com.example.algorithmvisualizer.ui.theme.Primary
import com.example.algorithmvisualizer.ui.theme.Secondary
import com.example.algorithmvisualizer.ui.theme.TextColor
import com.example.algorithmvisualizer.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * A more polished BFS visualization using a circular graph layout.
 *
 * - Displays an ElevatedCard with a detailed BFS explanation.
 * - Uses fixed-height sections for the explanation and graph visualization so that
 *   the entire interface can be scrolled if needed.
 * - Draws nodes as circles arranged in a circle with edges connecting them.
 * - Node colors are animated based on their BFS status.
 * - Playback controls (Play/Pause, Next, Previous, Reset) allow you to navigate the BFS steps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BFSView() {
    // Number of nodes in the graph.
    val nodeCount = 10

    // BFS steps state.
    val bfsSteps = remember { mutableStateListOf<BFSGraphStep>() }
    // Current step index.
    var currentStepIndex by remember { mutableStateOf(0) }
    // Auto-play flag.
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Initialize BFS steps once.
    LaunchedEffect(Unit) {
        val adjacency = buildFixedAdjacency(nodeCount)
        val steps = generateBFSSteps(adjacency, start = 0)
        bfsSteps.clear()
        bfsSteps.addAll(steps)
        currentStepIndex = 0
    }

    // Auto-play logic.
    LaunchedEffect(isPlaying, currentStepIndex) {
        if (isPlaying) {
            if (currentStepIndex < bfsSteps.lastIndex) {
                delay(600L)
                currentStepIndex++
            } else {
                isPlaying = false
            }
        }
    }

    // Get the current BFS step.
    val currentStep = bfsSteps.getOrNull(currentStepIndex)

    // Pre-calculate node colors in a valid composable scope.
    val colorStates = if (currentStep != null) {
        currentStep.visited.mapIndexed { i, visited ->
            val colorTarget = when {
                i == currentStep.currentNode -> Secondary  // current node.
                visited -> Primary                           // visited node.
                else -> LightGray                            // unvisited.
            }
            animateColorAsState(
                targetValue = colorTarget,
                animationSpec = tween(durationMillis = 600)
            )
        }
    } else emptyList()

    // Wrap the entire interface in a vertically scrollable Column.
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Explanation Card for BFS (fixed height).
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.explanation_text_bfs),
                    color = TextColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Justify
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Graph Visualization (fixed height for proper layout).
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .border(width = 2.dp, color = LightGray),
            shape = RoundedCornerShape(12.dp),
            color = White
        ) {
            // Center the content within the box
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val availableSize = min(maxWidth, maxHeight)
                val canvasSizePx = with(density) { availableSize.toPx() }
                val center = canvasSizePx / 2f
                val circleRadius = canvasSizePx * 0.4f

                // Compute node positions relative to the canvas center.
                val nodePositions = buildCircleLayout(
                    nodeCount = nodeCount,
                    centerX = center,
                    centerY = center,
                    radius = circleRadius
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw edges using the fixed adjacency list
                    val adjacency = buildFixedAdjacency(nodeCount)
                    adjacency.forEachIndexed { i, neighbors ->
                        val startPos = nodePositions[i]
                        neighbors.forEach { j ->
                            val endPos = nodePositions[j]
                            drawLine(
                                color = DarkGray,
                                start = startPos,
                                end = endPos,
                                strokeWidth = 4f
                            )
                        }
                    }

                    // Draw nodes as circles with animated colors
                    if (currentStep != null && colorStates.size == nodeCount) {
                        currentStep.visited.forEachIndexed { i, _ ->
                            val nodePos = nodePositions[i]
                            val animatedColor = colorStates[i].value
                            val nodeRadius = canvasSizePx * 0.06f

                            drawCircle(
                                color = animatedColor,
                                center = nodePos,
                                radius = nodeRadius
                            )

                            // Draw node index label in the center
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    color = White.toArgb()
                                    textSize = canvasSizePx * 0.05f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                canvas.nativeCanvas.drawText(
                                    i.toString(),
                                    nodePos.x,
                                    nodePos.y + (paint.textSize * 0.35f),
                                    paint
                                )
                            }
                        }
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Timeline slider.
        if (bfsSteps.isNotEmpty()) {
            Slider(
                value = currentStepIndex.toFloat(),
                onValueChange = { newValue ->
                    currentStepIndex = newValue.toInt()
                    isPlaying = false
                },
                valueRange = 0f..(bfsSteps.size - 1).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = LightGray
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback controls.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying)
                        stringResource(id = R.string.pause)
                    else
                        stringResource(id = R.string.play),
                    tint = Primary
                )
            }
            IconButton(onClick = {
                currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                isPlaying = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_previous),
                    contentDescription = stringResource(id = R.string.previous),
                    tint = Primary
                )
            }
            IconButton(onClick = {
                currentStepIndex = (currentStepIndex + 1).coerceAtMost(bfsSteps.lastIndex)
                isPlaying = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = stringResource(id = R.string.next),
                    tint = Primary
                )
            }
            IconButton(onClick = {
                isPlaying = false
                val adjacency = buildFixedAdjacency(nodeCount)
                val steps = generateBFSSteps(adjacency, start = 0)
                bfsSteps.clear()
                bfsSteps.addAll(steps)
                currentStepIndex = 0
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = stringResource(id = R.string.reset),
                    tint = Primary
                )
            }
        }
    }
}
