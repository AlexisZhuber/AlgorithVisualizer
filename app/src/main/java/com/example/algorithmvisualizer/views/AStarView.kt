package com.example.algorithmvisualizer.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.AStarGraphStep
import com.example.algorithmvisualizer.algorithm.WeightedEdge
import com.example.algorithmvisualizer.algorithm.buildNearbyWeightedAdjacency
import com.example.algorithmvisualizer.algorithm.generateAStarSteps
import com.example.algorithmvisualizer.components.ExplanationCard
import com.example.algorithmvisualizer.components.PlaybackControls
import com.example.algorithmvisualizer.components.TimelineSlider
import com.example.algorithmvisualizer.ui.theme.*
import com.example.algorithmvisualizer.util.buildConcentricCircleLayout
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * AStarView is a Composable function that displays a visualization of the A* algorithm.
 *
 * This view sets up a graph with nodes and weighted edges, generates a series of steps showing
 * the progression of the A* algorithm, and provides UI controls (play, pause, next, previous, reset)
 * for the user to interact with the visualization.
 */
@Composable
fun AStarView() {
    // Define total nodes.
    val nodeCount = 20

    // Graph configuration state.
    var startNode by remember { mutableStateOf(0) }
    var endNode by remember { mutableStateOf(0) }
    val adjacencyState = remember { mutableStateListOf<List<WeightedEdge>>() }
    val nodePositionsState = remember { mutableStateListOf<Pair<Float, Float>>() }

    // A* algorithm visualization state.
    val aStarSteps = remember { mutableStateListOf<AStarGraphStep>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    val connectionDistance = 0.35f

    // Checks if two nodes are connected using DFS.
    fun isConnected(adjacency: List<List<WeightedEdge>>, start: Int, end: Int): Boolean {
        val visited = MutableList(adjacency.size) { false }
        fun dfs(node: Int) {
            visited[node] = true
            for (edge in adjacency[node]) {
                if (!visited[edge.target]) dfs(edge.target)
            }
        }
        dfs(start)
        return visited[end]
    }

    // Resets the graph layout, connectivity, and generates new A* steps.
    fun resetAll() {
        var adjacency: List<MutableList<WeightedEdge>>
        do {
            val positions = buildConcentricCircleLayout(
                size = nodeCount,
                outerRadius = 0.5f,
                innerRadius = 0.3f
            )
            nodePositionsState.clear()
            nodePositionsState.addAll(positions)

            adjacency = buildNearbyWeightedAdjacency(positions, maxDistance = connectionDistance)
            startNode = Random.nextInt(nodeCount)
            endNode = Random.nextInt(nodeCount)
            while (endNode == startNode) {
                endNode = Random.nextInt(nodeCount)
            }
        } while (!isConnected(adjacency, startNode, endNode))

        adjacencyState.clear()
        adjacencyState.addAll(adjacency.map { it.toList() })

        aStarSteps.clear()
        val steps = generateAStarSteps(
            adjacencyState.toList(),
            nodePositionsState.toList(),
            start = startNode,
            end = endNode
        )
        aStarSteps.addAll(steps)
        currentStepIndex = 0
        isPlaying = false
    }

    // Trigger reset on first composition.
    LaunchedEffect(Unit) { resetAll() }
    // Auto-play effect.
    LaunchedEffect(isPlaying, currentStepIndex) {
        if (isPlaying) {
            if (currentStepIndex < aStarSteps.lastIndex) {
                delay(200L)
                currentStepIndex++
            } else {
                isPlaying = false
            }
        }
    }

    // Current step and animated color states.
    val currentStep = aStarSteps.getOrNull(currentStepIndex)
    val colorStates = currentStep?.gScores?.mapIndexed { i, _ ->
        val targetColor = when {
            i == currentStep.currentNode -> Secondary
            currentStep.visited[i] -> Primary
            else -> LightGray
        }
        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 600)
        )
    } ?: emptyList()

    val animatedBestStrokeWidth by animateFloatAsState(
        targetValue = if (currentStep?.pathEdges?.isNotEmpty() == true) 6f else 4f,
        animationSpec = tween(durationMillis = 600)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Reusable explanation card.
        ExplanationCard(
            explanationText = stringResource(id = R.string.explanation_text_astar) +
                    "\n" + stringResource(id = R.string.graph_info, nodeCount, startNode, endNode),
            cardHeight = 350.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Drawing area.
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
                val horizontalMarginPx = boxWidthPx * 0.06f
                val verticalMarginPx = boxHeightPx * 0.09f
                val effectiveWidth = boxWidthPx - 2 * horizontalMarginPx
                val effectiveHeight = boxHeightPx - 2 * verticalMarginPx

                val nodePositions = if (nodePositionsState.size == nodeCount) {
                    nodePositionsState.map { (x, y) ->
                        Offset(horizontalMarginPx + x * effectiveWidth, verticalMarginPx + y * effectiveHeight)
                    }
                } else emptyList()

                val originLabel = stringResource(id = R.string.origin_label)
                val destinationLabel = stringResource(id = R.string.destination_label)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (nodePositions.size == nodeCount && adjacencyState.size == nodeCount) {
                        val pathEdgeSet = currentStep?.pathEdges?.map {
                            if (it.first < it.second) it else Pair(it.second, it.first)
                        }?.toSet() ?: emptySet()

                        // Draw edges.
                        adjacencyState.forEachIndexed { i, edges ->
                            val startPos = nodePositions[i]
                            edges.forEach { edge ->
                                val endPos = nodePositions[edge.target]
                                val edgePair = if (i < edge.target) Pair(i, edge.target) else Pair(edge.target, i)
                                val edgeColor = if (edgePair in pathEdgeSet) Red else DarkGray
                                val strokeWidth = if (edgePair in pathEdgeSet) animatedBestStrokeWidth else 4f

                                drawLine(
                                    color = edgeColor,
                                    start = startPos,
                                    end = endPos,
                                    strokeWidth = strokeWidth
                                )

                                // Draw edge weight.
                                val midPoint = Offset((startPos.x + endPos.x) / 2f, (startPos.y + endPos.y) / 2f)
                                val dx = endPos.x - startPos.x
                                val dy = endPos.y - startPos.y
                                val length = sqrt(dx * dx + dy * dy)
                                val offsetDistance = boxWidthPx * 0.02f
                                var perpX = if (length != 0f) -dy / length else 0f
                                var perpY = if (length != 0f) dx / length else 0f
                                if (perpY > 0) {
                                    perpX = -perpX
                                    perpY = -perpY
                                }
                                val finalX = midPoint.x + perpX * offsetDistance
                                val finalY = midPoint.y + perpY * offsetDistance
                                if (i < edge.target) {
                                    drawIntoCanvas { canvas ->
                                        val paint = android.graphics.Paint().apply {
                                            color = edgeColor.toArgb()
                                            textSize = boxWidthPx * 0.03f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        canvas.nativeCanvas.drawText(edge.weight.toString(), finalX, finalY, paint)
                                    }
                                }
                            }
                        }

                        // Draw nodes.
                        val nodeRadius = boxWidthPx * 0.04f
                        if (currentStep != null && colorStates.size == nodeCount) {
                            currentStep.gScores.forEachIndexed { i, _ ->
                                val nodeCenter = nodePositions[i]
                                val animatedColor = colorStates[i].value
                                if (i == currentStep.currentNode) {
                                    drawCircle(
                                        color = Yellow.copy(alpha = 0.5f),
                                        center = nodeCenter,
                                        radius = nodeRadius * 1.3f
                                    )
                                }
                                drawCircle(color = animatedColor, center = nodeCenter, radius = nodeRadius)
                                drawCircle(
                                    color = DarkGray,
                                    center = nodeCenter,
                                    radius = nodeRadius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                                drawIntoCanvas { canvas ->
                                    val indexPaint = android.graphics.Paint().apply {
                                        color = White.toArgb()
                                        textSize = nodeRadius * 0.9f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    canvas.nativeCanvas.drawText(i.toString(), nodeCenter.x, nodeCenter.y - (nodeRadius * 0.09f), indexPaint)
                                    val cost = currentStep.gScores[i]
                                    val costText = if (cost == Int.MAX_VALUE) "∞" else cost.toString()
                                    val costPaint = android.graphics.Paint().apply {
                                        color = White.toArgb()
                                        textSize = nodeRadius * 0.6f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    canvas.nativeCanvas.drawText(costText, nodeCenter.x, nodeCenter.y + (nodeRadius * 0.8f), costPaint)
                                }
                                if (i == startNode) {
                                    drawIntoCanvas { canvas ->
                                        val labelPaint = android.graphics.Paint().apply {
                                            color = Green.toArgb()
                                            textSize = nodeRadius * 0.6f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        canvas.nativeCanvas.drawText(originLabel, nodeCenter.x, nodeCenter.y - nodeRadius - labelPaint.textSize, labelPaint)
                                    }
                                } else if (i == endNode) {
                                    drawIntoCanvas { canvas ->
                                        val labelPaint = android.graphics.Paint().apply {
                                            color = Blue.toArgb()
                                            textSize = nodeRadius * 0.6f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        canvas.nativeCanvas.drawText(destinationLabel, nodeCenter.x, nodeCenter.y + nodeRadius + labelPaint.textSize * 1.2f, labelPaint)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reusable TimelineSlider.
        if (aStarSteps.isNotEmpty()) {
            TimelineSlider(
                currentStep = currentStepIndex.toFloat(),
                maxStep = (aStarSteps.size - 1).toFloat(),
                onStepChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, aStarSteps.lastIndex)
                    isPlaying = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reusable PlaybackControls.
        if (aStarSteps.isNotEmpty()) {
            PlaybackControls(
                isPlaying = isPlaying,
                onPlayPauseToggle = { isPlaying = !isPlaying },
                onPrevious = {
                    currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                    isPlaying = false
                },
                onNext = {
                    currentStepIndex = (currentStepIndex + 1).coerceAtMost(aStarSteps.lastIndex)
                    isPlaying = false
                },
                onReset = { resetAll() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final results card.
        if (currentStepIndex == aStarSteps.lastIndex && aStarSteps.isNotEmpty()) {
            val finalStep = aStarSteps.last()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.results_title_astar),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.results_description_astar),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(id = R.string.table_header_node),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(id = R.string.table_header_cost),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Divider(color = DarkGray, thickness = 1.dp)
                    finalStep.gScores.forEachIndexed { index, cost ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "${stringResource(id = R.string.table_row_node)} $index",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (cost == Int.MAX_VALUE) "∞" else cost.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Divider(color = DarkGray, thickness = 1.dp)
                    finalStep.iterationCount?.let { iterations ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.table_header_iterations),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = iterations.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.results_footer_astar),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkGray
                    )
                }
            }
        }
    }
}
