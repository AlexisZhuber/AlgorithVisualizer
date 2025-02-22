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
import androidx.compose.ui.unit.sp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.BFSGraphStep
import com.example.algorithmvisualizer.algorithm.buildRandomAdjacency
import com.example.algorithmvisualizer.algorithm.generateBFSSteps
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

    /**
     * Resets the visualization by:
     * 1. Generating a new node layout using a concentric circle layout.
     * 2. Creating a new random graph with at most 2 connections per node.
     * 3. Running BFS from the start node and recording each step.
     */
    fun resetAll() {
        // Create a node layout using a concentric circle layout (normalized positions).
        val positions = buildConcentricCircleLayout(
            size = nodeCount,
            outerRadius = 0.5f, // Adjust outer circle separation if needed.
            innerRadius = 0.3f  // Adjust inner circle separation if needed.
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

    // Restart the visualization when the composable is first launched.
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

    // Animate node colors based on their state:
    // - Current node: use Secondary color (with glow effect)
    // - Visited nodes: use Primary color
    // - Unvisited nodes: use LightGray color
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
        // Explanation card that displays the BFS description and graph information.
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCard)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.explanation_text_bfs),
                    color = TextColor,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.graph_info_bfs, nodeCount, startNode),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkGray
                )
            }
        }

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
                val horizontalMarginFraction = 0.1f
                val verticalMarginFraction = 0.09f
                val horizontalMarginPx = boxWidthPx * horizontalMarginFraction
                val verticalMarginPx = boxHeightPx * verticalMarginFraction
                val effectiveWidth = boxWidthPx - 2 * horizontalMarginPx
                val effectiveHeight = boxHeightPx - 2 * verticalMarginPx

                // Map the normalized node positions to actual canvas positions.
                val nodePositions = if (nodePositionsState.size == nodeCount) {
                    nodePositionsState.map { (x, y) ->
                        Offset(horizontalMarginPx + x * effectiveWidth, verticalMarginPx + y * effectiveHeight)
                    }
                } else emptyList()

                // Retrieve the label for the origin from string resources.
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

                                // If this node is the current node, draw a glow effect.
                                if (i == currentStep.currentNode) {
                                    drawCircle(
                                        color = Secondary.copy(alpha = 0.5f),
                                        center = nodeCenter,
                                        radius = nodeRadius * 1.3f
                                    )
                                }
                                // Draw the node circle.
                                drawCircle(
                                    color = animatedColor,
                                    center = nodeCenter,
                                    radius = nodeRadius
                                )
                                // Draw node border.
                                drawCircle(
                                    color = DarkGray,
                                    center = nodeCenter,
                                    radius = nodeRadius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                                // Draw the node's ID inside the circle and the visit order (if available) below the node.
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
                                // Label the starting node with "ORIGIN" from the string resource.
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

        // Slider to control the animation timeline.
        if (bfsSteps.isNotEmpty()) {
            Slider(
                value = currentStepIndex.toFloat(),
                onValueChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, bfsSteps.lastIndex)
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

        // Playback controls: Play/Pause, Previous, Next, and Reset.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = stringResource(id = if (isPlaying) R.string.pause else R.string.play),
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
                resetAll()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = stringResource(id = R.string.reset),
                    tint = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final results card that displays the BFS results.
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
