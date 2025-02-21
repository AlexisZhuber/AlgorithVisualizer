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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.DijkstraGraphStep
import com.example.algorithmvisualizer.algorithm.WeightedEdge
import com.example.algorithmvisualizer.algorithm.buildNearbyWeightedAdjacency
import com.example.algorithmvisualizer.algorithm.generateDijkstraSteps
import com.example.algorithmvisualizer.ui.theme.*
import com.example.algorithmvisualizer.util.buildConcentricCircleLayout
import kotlinx.coroutines.delay
import kotlin.math.abs
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
    val nodeCount = 15

    // Graph states.
    var startNode by remember { mutableStateOf(0) }
    var endNode by remember { mutableStateOf(0) }
    val adjacencyState = remember { mutableStateListOf<List<WeightedEdge>>() }
    val nodePositionsState = remember { mutableStateListOf<Pair<Float, Float>>() }

    // Dijkstra states.
    val dijkstraSteps = remember { mutableStateListOf<DijkstraGraphStep>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Maximum connection distance (normalized). Change this to adjust node connectivity.
    val connectionDistance = 0.4f

    // Check connectivity between two nodes (using DFS).
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

    // Reset visualization.
    fun resetAll() {
        var adjacency: List<MutableList<WeightedEdge>>
        do {
            // Generate layout with increased separation.
            val positions = buildConcentricCircleLayout(
                size = nodeCount,
                outerRadius = 0.5f,  // Modify for outer circle separation.
                innerRadius = 0.3f   // Modify for inner circle separation.
            )
            nodePositionsState.clear()
            nodePositionsState.addAll(positions)

            // Generate deterministic connections for nodes within connectionDistance.
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

    // Animate stroke width for edges on the best route.
    val animatedBestStrokeWidth by animateFloatAsState(
        targetValue = if (currentStep?.pathEdges?.isNotEmpty() == true) 6f else 4f,
        animationSpec = tween(durationMillis = 600)
    )

    // Main UI.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Explanation card.
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundCard
            )
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.explanation_text_dijkstra),
                    color = TextColor,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.graph_info, nodeCount, startNode, endNode),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drawing area.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)  // Height of drawing box.
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
                // Define separate horizontal and vertical margins.
                // To change the margins, modify these fractions.
                val horizontalMarginFraction = 0.1f  // 10% of width on each side.
                val verticalMarginFraction = 0.09f    // 5% of height on top and bottom.
                val horizontalMarginPx = boxWidthPx * horizontalMarginFraction
                val verticalMarginPx = boxHeightPx * verticalMarginFraction

                val effectiveWidth = boxWidthPx - 2 * horizontalMarginPx
                val effectiveHeight = boxHeightPx - 2 * verticalMarginPx

                // Map normalized positions [0,1] to the effective drawing area.
                val nodePositions = if (nodePositionsState.size == nodeCount) {
                    nodePositionsState.map { (x, y) ->
                        Offset(
                            horizontalMarginPx + x * effectiveWidth,
                            verticalMarginPx + y * effectiveHeight
                        )
                    }
                } else {
                    emptyList()
                }
                // Definir los textos para origen y destino antes del Canvas:
                val originLabel = stringResource(id = R.string.origin_label)
                val destinationLabel = stringResource(id = R.string.destination_label)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (nodePositions.size == nodeCount && adjacencyState.size == nodeCount) {
                        // Build set of edges that form the best route.
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

                                // Compute midpoint to draw weight.
                                val midPoint = Offset(
                                    (startPos.x + endPos.x) / 2f,
                                    (startPos.y + endPos.y) / 2f
                                )
                                val dx = endPos.x - startPos.x
                                val dy = endPos.y - startPos.y
                                val length = kotlin.math.sqrt(dx * dx + dy * dy)
                                val offsetDistance = boxWidthPx * 0.015f

                                // Calculate a unit perpendicular vector.
                                var perpX = if (length != 0f) -dy / length else 0f
                                var perpY = if (length != 0f) dx / length else 0f

                                // Optionally, adjust the perpendicular direction so the text is always above the line.
                                // For example, ensure that the y-component is negative.
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
                                        canvas.nativeCanvas.drawText(
                                            edge.weight.toString(),
                                            finalX,
                                            finalY,
                                            paint
                                        )
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

                                // Draw "glow" around current node.
                                if (i == currentStep.currentNode) {
                                    drawCircle(
                                        color = Yellow.copy(alpha = 0.5f),
                                        center = nodeCenter,
                                        radius = nodeRadius * 1.3f
                                    )
                                }
                                // Draw node filled.
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

                                // Draw the node's ID and its accumulated cost.
                                drawIntoCanvas { canvas ->
                                    val indexPaint = android.graphics.Paint().apply {
                                        color = White.toArgb()
                                        textSize = nodeRadius * 0.9f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    val costPaint = android.graphics.Paint().apply {
                                        color = White.toArgb()
                                        textSize = nodeRadius * 0.6f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    // Number inside the circle represents the node ID.
                                    canvas.nativeCanvas.drawText(
                                        i.toString(),
                                        nodeCenter.x,
                                        nodeCenter.y - (nodeRadius * 0.09f),
                                        indexPaint
                                    )
                                    // Number below the node represents the accumulated cost (shortest distance from the origin).
                                    val d = currentStep.distances[i]
                                    val costText = if (d == Int.MAX_VALUE) "∞" else d.toString()
                                    canvas.nativeCanvas.drawText(
                                        costText,
                                        nodeCenter.x,
                                        nodeCenter.y + (nodeRadius * 0.8f),
                                        costPaint
                                    )
                                }

                                // Draw labels for origin and destination.
                                if (i == startNode) {
                                    drawIntoCanvas { canvas ->
                                        val labelPaint = android.graphics.Paint().apply {
                                            color = Green.toArgb()
                                            textSize = nodeRadius * 0.6f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        // Label "ORIGEN" for the starting node.
                                        canvas.nativeCanvas.drawText(
                                            originLabel,
                                            nodeCenter.x,
                                            nodeCenter.y - nodeRadius - labelPaint.textSize,
                                            labelPaint
                                        )
                                    }
                                } else if (i == endNode) {
                                    drawIntoCanvas { canvas ->
                                        val labelPaint = android.graphics.Paint().apply {
                                            color = Blue.toArgb()
                                            textSize = nodeRadius * 0.6f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        // Label "DESTINO" for the destination node.
                                        canvas.nativeCanvas.drawText(
                                            destinationLabel,
                                            nodeCenter.x,
                                            nodeCenter.y + nodeRadius + labelPaint.textSize * 1.2f,
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

        // Slider for animation control.
        if (dijkstraSteps.isNotEmpty()) {
            Slider(
                value = currentStepIndex.toFloat(),
                onValueChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, dijkstraSteps.lastIndex)
                    isPlaying = false
                },
                valueRange = 0f..(dijkstraSteps.size - 1).toFloat(),
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
                currentStepIndex = (currentStepIndex + 1).coerceAtMost(dijkstraSteps.lastIndex)
                isPlaying = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = stringResource(id = R.string.next),
                    tint = Primary
                )
            }
            IconButton(onClick = { resetAll() }) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = stringResource(id = R.string.reset),
                    tint = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final results card with detailed explanation.
        if (currentStepIndex == dijkstraSteps.lastIndex && dijkstraSteps.isNotEmpty()) {
            val finalStep = dijkstraSteps.last()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BackgroundCard
                )
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
