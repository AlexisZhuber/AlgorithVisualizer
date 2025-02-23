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
import com.example.algorithmvisualizer.algorithm.DijkstraGraphStep
import com.example.algorithmvisualizer.algorithm.WeightedEdge
import com.example.algorithmvisualizer.algorithm.buildNearbyWeightedAdjacency
import com.example.algorithmvisualizer.algorithm.generateDijkstraSteps
import com.example.algorithmvisualizer.components.ExplanationCard
import com.example.algorithmvisualizer.components.PlaybackControls
import com.example.algorithmvisualizer.components.TimelineSlider
import com.example.algorithmvisualizer.ui.theme.*
import com.example.algorithmvisualizer.util.buildConcentricCircleLayout
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * DijkstraView displays Dijkstra’s algorithm using a concentric circle layout.
 *
 * Improvements:
 * - Drawing area (Canvas) is 600 dp high, with separate horizontal and vertical margins.
 *   *To change the margins, modify horizontalMarginFraction and verticalMarginFraction.*
 * - The variable connectionDistance defines the maximum distance (normalized) for nodes to connect.
 *   *To change which nodes are connected, modify connectionDistance in resetAll().*
 * - For edges that are nearly vertical, the weight is drawn with an x-offset so that it appears beside the line.
 * - The final results card is descriptive, explaining the number inside each circle (node ID) and the number below (accumulated cost from the origin),
 *   and explains how the shortest route was determined.
 *
 * All strings, colors, and typography are taken from resource files.
 */
@Composable
fun DijkstraView() {
    val nodeCount = 20

    // Graph state.
    var startNode by remember { mutableStateOf(0) }
    var endNode by remember { mutableStateOf(0) }
    val adjacencyState = remember { mutableStateListOf<List<WeightedEdge>>() }
    val nodePositionsState = remember { mutableStateListOf<Pair<Float, Float>>() }

    // Dijkstra visualization state.
    val dijkstraSteps = remember { mutableStateListOf<DijkstraGraphStep>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    // Maximum normalized connection distance.
    val connectionDistance = 0.35f

    // Helper function to check connectivity between two nodes using DFS.
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

    // Reset function: generate a new layout, graph, and Dijkstra steps.
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

        dijkstraSteps.clear()
        val steps = generateDijkstraSteps(adjacencyState.toList(), start = startNode, end = endNode)
        dijkstraSteps.addAll(steps)
        currentStepIndex = 0
        isPlaying = false
    }

    LaunchedEffect(Unit) { resetAll() }
    LaunchedEffect(isPlaying, currentStepIndex) {
        if (isPlaying) {
            if (currentStepIndex < dijkstraSteps.lastIndex) {
                delay(200L)
                currentStepIndex++
            } else {
                isPlaying = false
            }
        }
    }

    val currentStep = dijkstraSteps.getOrNull(currentStepIndex)
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
        // Explanation card (reusable component).
        ExplanationCard(
            explanationText = stringResource(id = R.string.explanation_text_dijkstra) +
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
                                val midPoint = Offset((startPos.x + endPos.x) / 2f, (startPos.y + endPos.y) / 2f)
                                val dx = endPos.x - startPos.x
                                val dy = endPos.y - startPos.y
                                val length = kotlin.math.sqrt(dx * dx + dy * dy)
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
                            currentStep.visited.forEachIndexed { i, _ ->
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
                                    val d = currentStep.distances[i]
                                    val costText = if (d == Int.MAX_VALUE) "∞" else d.toString()
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

        // Reusable TimelineSlider component.
        if (dijkstraSteps.isNotEmpty()) {
            TimelineSlider(
                currentStep = currentStepIndex.toFloat(),
                maxStep = (dijkstraSteps.size - 1).toFloat(),
                onStepChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, dijkstraSteps.lastIndex)
                    isPlaying = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reusable PlaybackControls component.
        if (dijkstraSteps.isNotEmpty()) {
            PlaybackControls(
                isPlaying = isPlaying,
                onPlayPauseToggle = { isPlaying = !isPlaying },
                onPrevious = {
                    currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                    isPlaying = false
                },
                onNext = {
                    currentStepIndex = (currentStepIndex + 1).coerceAtMost(dijkstraSteps.lastIndex)
                    isPlaying = false
                },
                onReset = { resetAll() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final results card.
        if (currentStepIndex == dijkstraSteps.lastIndex && dijkstraSteps.isNotEmpty()) {
            val finalStep = dijkstraSteps.last()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(id = R.string.results_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.results_description),
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
                    finalStep.distances.forEachIndexed { index, distance ->
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
                                text = if (distance == Int.MAX_VALUE) "∞" else distance.toString(),
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
                        text = stringResource(id = R.string.results_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkGray
                    )
                }
            }
        }
    }
}
