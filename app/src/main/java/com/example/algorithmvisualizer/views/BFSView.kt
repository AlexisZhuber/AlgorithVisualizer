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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.BFSGraphStep
import com.example.algorithmvisualizer.algorithm.buildRandomAdjacency
import com.example.algorithmvisualizer.algorithm.generateBFSSteps
import com.example.algorithmvisualizer.components.ExplanationCard
import com.example.algorithmvisualizer.components.PlaybackControls
import com.example.algorithmvisualizer.components.TimelineSlider
import com.example.algorithmvisualizer.ui.theme.*
import com.example.algorithmvisualizer.util.buildConcentricCircleLayout
import kotlinx.coroutines.delay

/**
 * BFSView displays a visualization of the Breadth-First Search (BFS) algorithm.
 *
 * This view mimics the Dijkstra interface style by:
 * - Using an explanation card that displays texts from string resources.
 * - Arranging the nodes on the canvas using a concentric circle layout.
 * - Generating a random graph in which each node has a maximum of 2 connections.
 * - Displaying BFS animation steps with playback controls and a final results card
 *   that shows the order in which nodes were visited.
 */
@Composable
fun BFSView() {
    // Total number of nodes in the graph.
    val nodeCount = 15
    // Define the starting node for BFS.
    val startNode = 0

    // States for the graph structure and node positions.
    val adjacencyState = remember { mutableStateListOf<List<Int>>() }
    val nodePositionsState = remember { mutableStateListOf<Pair<Float, Float>>() }

    // States for BFS algorithm visualization.
    val bfsSteps = remember { mutableStateListOf<BFSGraphStep>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    // Reset function to generate new node positions, graph connectivity, and BFS steps.
    fun resetAll() {
        // Generate a node layout using a concentric circle layout (normalized positions).
        val positions = buildConcentricCircleLayout(
            size = nodeCount,
            outerRadius = 0.5f,
            innerRadius = 0.3f
        )
        nodePositionsState.clear()
        nodePositionsState.addAll(positions)

        // Generate random connectivity with each node having at most 2 connections.
        val adjacency = buildRandomAdjacency(nodeCount)
        adjacencyState.clear()
        adjacencyState.addAll(adjacency)

        // Generate BFS steps starting from the start node.
        bfsSteps.clear()
        val steps = generateBFSSteps(adjacency, start = startNode)
        bfsSteps.addAll(steps)
        currentStepIndex = 0
        isPlaying = false
    }

    // Launch reset on first composition.
    LaunchedEffect(Unit) { resetAll() }
    // Auto-play effect for the BFS animation.
    LaunchedEffect(isPlaying, currentStepIndex) {
        if (isPlaying) {
            if (currentStepIndex < bfsSteps.lastIndex) {
                delay(200L)
                currentStepIndex++
            } else {
                isPlaying = false
            }
        }
    }

    // Get the current BFS step.
    val currentStep = bfsSteps.getOrNull(currentStepIndex)
    // Animate node colors based on their state.
    val colorStates = currentStep?.visited?.mapIndexed { i, visited ->
        val targetColor = when {
            i == currentStep.currentNode -> Secondary
            visited -> Primary
            else -> LightGray
        }
        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 600)
        )
    } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Reusable ExplanationCard displaying the BFS description and graph info.
        ExplanationCard(
            explanationText = stringResource(id = R.string.explanation_text_bfs) +
                    "\n" + stringResource(id = R.string.graph_info_bfs, nodeCount, startNode),
            cardHeight = 350.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Drawing area for the graph.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)
                .border(width = 2.dp, color = LightGray),
            shape = RoundedCornerShape(12.dp),
            color = White
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val boxWidthPx = with(density) { maxWidth.toPx() }
                val boxHeightPx = with(density) { maxHeight.toPx() }
                // Define margins as fractions of the drawing area.
                val horizontalMarginPx = boxWidthPx * 0.1f
                val verticalMarginPx = boxHeightPx * 0.09f
                val effectiveWidth = boxWidthPx - 2 * horizontalMarginPx
                val effectiveHeight = boxHeightPx - 2 * verticalMarginPx

                // Map the normalized node positions to actual canvas positions.
                val nodePositions = if (nodePositionsState.size == nodeCount) {
                    nodePositionsState.map { (x, y) ->
                        Offset(horizontalMarginPx + x * effectiveWidth, verticalMarginPx + y * effectiveHeight)
                    }
                } else emptyList()

                // Retrieve the label for the origin.
                val originLabel = stringResource(id = R.string.origin_label)
                // Draw the graph.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw edges (lines between connected nodes).
                    if (nodePositions.size == nodeCount && adjacencyState.size == nodeCount) {
                        adjacencyState.forEachIndexed { i, neighbors ->
                            val startPos = nodePositions[i]
                            neighbors.forEach { j ->
                                val endPos = nodePositions.getOrNull(j)
                                if (endPos != null) {
                                    drawLine(
                                        color = DarkGray,
                                        start = startPos,
                                        end = endPos,
                                        strokeWidth = 4f
                                    )
                                }
                            }
                        }
                        // Draw nodes.
                        val nodeRadius = boxWidthPx * 0.04f
                        if (currentStep != null && colorStates.size == nodeCount) {
                            currentStep.visited.forEachIndexed { i, _ ->
                                val nodeCenter = nodePositions[i]
                                val animatedColor = colorStates[i].value

                                // Draw glow for the current node.
                                if (i == currentStep.currentNode) {
                                    drawCircle(
                                        color = Secondary.copy(alpha = 0.5f),
                                        center = nodeCenter,
                                        radius = nodeRadius * 1.3f
                                    )
                                }
                                // Draw node circle and border.
                                drawCircle(color = animatedColor, center = nodeCenter, radius = nodeRadius)
                                drawCircle(
                                    color = DarkGray,
                                    center = nodeCenter,
                                    radius = nodeRadius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )

                                // Draw node ID and visit order.
                                drawIntoCanvas { canvas ->
                                    val indexPaint = android.graphics.Paint().apply {
                                        color = White.toArgb()
                                        textSize = nodeRadius * 0.9f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    canvas.nativeCanvas.drawText(
                                        i.toString(),
                                        nodeCenter.x,
                                        nodeCenter.y - (nodeRadius * 0.1f),
                                        indexPaint
                                    )
                                    val orderIndex = currentStep.visitOrder.indexOf(i)
                                    val orderText = if (orderIndex >= 0) (orderIndex + 1).toString() else ""
                                    val orderPaint = android.graphics.Paint().apply {
                                        color = White.toArgb()
                                        textSize = nodeRadius * 0.6f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    canvas.nativeCanvas.drawText(
                                        orderText,
                                        nodeCenter.x,
                                        nodeCenter.y + (nodeRadius * 0.7f),
                                        orderPaint
                                    )
                                }
                                // Label the starting node.
                                if (i == startNode) {
                                    drawIntoCanvas { canvas ->
                                        val labelPaint = android.graphics.Paint().apply {
                                            color = Green.toArgb()
                                            textSize = nodeRadius * 0.6f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        canvas.nativeCanvas.drawText(
                                            originLabel,
                                            nodeCenter.x,
                                            nodeCenter.y - nodeRadius - labelPaint.textSize,
                                            labelPaint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render TimelineSlider only if steps are available.
        if (bfsSteps.isNotEmpty()) {
            TimelineSlider(
                currentStep = currentStepIndex.toFloat(),
                maxStep = (bfsSteps.size - 1).toFloat(),
                onStepChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, bfsSteps.lastIndex)
                    isPlaying = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render PlaybackControls if steps are available.
        if (bfsSteps.isNotEmpty()) {
            PlaybackControls(
                isPlaying = isPlaying,
                onPlayPauseToggle = { isPlaying = !isPlaying },
                onPrevious = {
                    currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                    isPlaying = false
                },
                onNext = {
                    currentStepIndex = (currentStepIndex + 1).coerceAtMost(bfsSteps.lastIndex)
                    isPlaying = false
                },
                onReset = { resetAll() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final results card: displays the BFS visit order.
        if (currentStepIndex == bfsSteps.lastIndex && bfsSteps.isNotEmpty()) {
            val finalStep = bfsSteps.last()
            val orderString = finalStep.visitOrder.joinToString(separator = ", ")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.results_title_bfs),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.results_description_bfs, orderString),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                }
            }
        }
    }
}
