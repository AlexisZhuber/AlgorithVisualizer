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
import com.example.algorithmvisualizer.algorithm.AStarGraphStep
import com.example.algorithmvisualizer.algorithm.WeightedEdge
import com.example.algorithmvisualizer.algorithm.buildNearbyWeightedAdjacency
import com.example.algorithmvisualizer.algorithm.generateAStarSteps
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
    // Define the total number of nodes in the graph.
    val nodeCount = 20

    // States for graph configuration.
    var startNode by remember { mutableStateOf(0) }
    var endNode by remember { mutableStateOf(0) }
    // List to hold adjacency lists for each node.
    val adjacencyState = remember { mutableStateListOf<List<WeightedEdge>>() }
    // List to hold normalized positions for each node.
    val nodePositionsState = remember { mutableStateListOf<Pair<Float, Float>>() }

    // States for the A* algorithm execution.
    val aStarSteps = remember { mutableStateListOf<AStarGraphStep>() }
    // Index of the current step in the A* visualization.
    var currentStepIndex by remember { mutableStateOf(0) }
    // Flag to control the automatic playback of the algorithm.
    var isPlaying by remember { mutableStateOf(false) }
    // Remember a coroutine scope for launching asynchronous tasks.
    val scope = rememberCoroutineScope()

    // Maximum normalized distance to connect nodes. Nodes within this distance will be connected.
    val connectionDistance = 0.35f

    /**
     * Checks if two nodes in the graph are connected using Depth-First Search (DFS).
     *
     * @param adjacency The graph's adjacency list.
     * @param start The starting node index.
     * @param end The target node index.
     * @return True if there is a path from start to end, false otherwise.
     */
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

    /**
     * Resets the graph and A* algorithm visualization.
     *
     * This function generates a new node layout, builds a new adjacency list ensuring that the start
     * and end nodes are connected, and generates a new sequence of A* algorithm steps.
     */
    fun resetAll() {
        var adjacency: List<MutableList<WeightedEdge>>
        do {
            // Generate a layout using two concentric circles.
            val positions = buildConcentricCircleLayout(
                size = nodeCount,
                outerRadius = 0.5f,  // Adjusts separation for the outer circle.
                innerRadius = 0.3f   // Adjusts separation for the inner circle.
            )
            nodePositionsState.clear()
            nodePositionsState.addAll(positions)

            // Build deterministic connections between nodes that are near each other.
            adjacency = buildNearbyWeightedAdjacency(positions, maxDistance = connectionDistance)
            // Randomly select start and end nodes.
            startNode = Random.nextInt(nodeCount)
            endNode = Random.nextInt(nodeCount)
            while (endNode == startNode) {
                endNode = Random.nextInt(nodeCount)
            }
        } while (!isConnected(adjacency, startNode, endNode)) // Ensure that start and end are connected.

        // Update the state with the new adjacency list.
        adjacencyState.clear()
        adjacencyState.addAll(adjacency.map { it.toList() })

        // Generate and store the sequence of A* algorithm steps.
        aStarSteps.clear()
        val steps = generateAStarSteps(
            adjacencyState.toList(),
            nodePositionsState.toList(),
            start = startNode,
            end = endNode
        )
        aStarSteps.addAll(steps)
        // Reset the current step index and stop playback.
        currentStepIndex = 0
        isPlaying = false
    }

    // Trigger a reset when the composable is first launched.
    LaunchedEffect(Unit) { resetAll() }
    // Update the current step index when the animation is playing.
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

    // Get the current visualization step.
    val currentStep = aStarSteps.getOrNull(currentStepIndex)
    // Animate the color changes for each node based on its status (current, visited, or unvisited).
    val colorStates = currentStep?.gScores?.mapIndexed { i, _ ->
        val targetColor = when {
            i == currentStep.currentNode -> Secondary   // Highlight current node.
            currentStep.visited[i] -> Primary               // Color visited nodes.
            else -> LightGray                               // Default color.
        }
        animateColorAsState(
            targetValue = targetColor,
            animationSpec = tween(durationMillis = 600)
        )
    } ?: emptyList()

    // Animate the stroke width for the optimal path edges.
    val animatedBestStrokeWidth by animateFloatAsState(
        targetValue = if (currentStep?.pathEdges?.isNotEmpty() == true) 6f else 4f,
        animationSpec = tween(durationMillis = 600)
    )

    // Main UI layout container.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Explanatory card that shows details about the A* algorithm.
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = BackgroundCard
            )
        ) {
            // Column inside the card with its own vertical scrolling.
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Display the algorithm explanation text from string resources.
                Text(
                    text = stringResource(id = R.string.explanation_text_astar),
                    color = TextColor,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Display graph information including node count, start node, and end node.
                Text(
                    text = stringResource(id = R.string.graph_info, nodeCount, startNode, endNode),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Drawing area where the graph and algorithm visualization are rendered.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp)  // Set the height for the drawing area.
                .border(width = 2.dp, color = LightGray),
            shape = RoundedCornerShape(12.dp),
            color = White
        ) {
            // Box to center the canvas and manage layout constraints.
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Get density to convert dp to pixels.
                val density = LocalDensity.current
                val boxWidthPx = with(density) { maxWidth.toPx() }
                val boxHeightPx = with(density) { maxHeight.toPx() }
                // Define horizontal and vertical margins as fractions of the box dimensions.
                val horizontalMarginFraction = 0.06f
                val verticalMarginFraction = 0.09f
                val horizontalMarginPx = boxWidthPx * horizontalMarginFraction
                val verticalMarginPx = boxHeightPx * verticalMarginFraction

                // Calculate the effective drawing area dimensions.
                val effectiveWidth = boxWidthPx - 2 * horizontalMarginPx
                val effectiveHeight = boxHeightPx - 2 * verticalMarginPx

                // Map normalized node positions ([0,1] range) to actual coordinates in the drawing area.
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
                // Retrieve labels for origin and destination from string resources.
                val originLabel = stringResource(id = R.string.origin_label)
                val destinationLabel = stringResource(id = R.string.destination_label)
                // Canvas for drawing the graph.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Only draw if we have the expected number of node positions and adjacency lists.
                    if (nodePositions.size == nodeCount && adjacencyState.size == nodeCount) {
                        // Get the set of edges that form the optimal path from the current step.
                        val pathEdgeSet = currentStep?.pathEdges?.map {
                            if (it.first < it.second) it else Pair(it.second, it.first)
                        }?.toSet() ?: emptySet()

                        // Draw all the edges in the graph.
                        adjacencyState.forEachIndexed { i, edges ->
                            val startPos = nodePositions[i]
                            edges.forEach { edge ->
                                val endPos = nodePositions[edge.target]
                                // Create an edge pair with the smaller index first.
                                val edgePair = if (i < edge.target) Pair(i, edge.target) else Pair(edge.target, i)
                                // Use red color for optimal path edges, dark gray for others.
                                val edgeColor = if (edgePair in pathEdgeSet) Red else DarkGray
                                // Use animated stroke width for optimal path edges.
                                val strokeWidth = if (edgePair in pathEdgeSet) animatedBestStrokeWidth else 4f

                                drawLine(
                                    color = edgeColor,
                                    start = startPos,
                                    end = endPos,
                                    strokeWidth = strokeWidth
                                )

                                // Calculate the midpoint of the edge to display its weight.
                                val midPoint = Offset(
                                    (startPos.x + endPos.x) / 2f,
                                    (startPos.y + endPos.y) / 2f
                                )
                                // Determine a perpendicular offset for the text.
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
                                // Draw the weight only for one direction to avoid duplicate text.
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

                        // Draw the nodes.
                        val nodeRadius = boxWidthPx * 0.04f
                        if (currentStep != null && colorStates.size == nodeCount) {
                            currentStep.gScores.forEachIndexed { i, _ ->
                                val nodeCenter = nodePositions[i]
                                val animatedColor = colorStates[i].value

                                // Draw a glowing circle for the current node.
                                if (i == currentStep.currentNode) {
                                    drawCircle(
                                        color = Yellow.copy(alpha = 0.5f),
                                        center = nodeCenter,
                                        radius = nodeRadius * 1.3f
                                    )
                                }
                                // Draw the filled circle for the node.
                                drawCircle(
                                    color = animatedColor,
                                    center = nodeCenter,
                                    radius = nodeRadius
                                )
                                // Draw the border around the node.
                                drawCircle(
                                    color = DarkGray,
                                    center = nodeCenter,
                                    radius = nodeRadius,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )

                                // Draw the node ID and its accumulated cost.
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
                                    canvas.nativeCanvas.drawText(
                                        i.toString(),
                                        nodeCenter.x,
                                        nodeCenter.y - (nodeRadius * 0.09f),
                                        indexPaint
                                    )
                                    val cost = currentStep.gScores[i]
                                    val costText = if (cost == Int.MAX_VALUE) "∞" else cost.toString()
                                    canvas.nativeCanvas.drawText(
                                        costText,
                                        nodeCenter.x,
                                        nodeCenter.y + (nodeRadius * 0.8f),
                                        costPaint
                                    )
                                }

                                // Draw labels for the start and end nodes.
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
                                } else if (i == endNode) {
                                    drawIntoCanvas { canvas ->
                                        val labelPaint = android.graphics.Paint().apply {
                                            color = Blue.toArgb()
                                            textSize = nodeRadius * 0.6f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
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

        // Slider to control the animation step of the A* algorithm.
        if (aStarSteps.isNotEmpty()) {
            Slider(
                value = currentStepIndex.toFloat(),
                onValueChange = { newValue ->
                    // Update the current step index based on slider position.
                    currentStepIndex = newValue.toInt().coerceIn(0, aStarSteps.lastIndex)
                    // Pause the animation when the user moves the slider.
                    isPlaying = false
                },
                valueRange = 0f..(aStarSteps.size - 1).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = LightGray
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback controls for the animation (play/pause, previous, next, reset).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Toggle play/pause.
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = stringResource(id = if (isPlaying) R.string.pause else R.string.play),
                    tint = Primary
                )
            }
            // Move to the previous step.
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
            // Move to the next step.
            IconButton(onClick = {
                currentStepIndex = (currentStepIndex + 1).coerceAtMost(aStarSteps.lastIndex)
                isPlaying = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = stringResource(id = R.string.next),
                    tint = Primary
                )
            }
            // Reset the visualization.
            IconButton(onClick = { resetAll() }) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = stringResource(id = R.string.reset),
                    tint = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final results card showing details of the A* algorithm execution.
        if (currentStepIndex == aStarSteps.lastIndex && aStarSteps.isNotEmpty()) {
            val finalStep = aStarSteps.last()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BackgroundCard
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title for the final results.
                    Text(
                        text = stringResource(id = R.string.results_title_astar),
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Description of the results.
                    Text(
                        text = stringResource(id = R.string.results_description_astar),
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Header row for the results table.
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
                    // Display each node and its accumulated cost.
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
                    // Show the number of iterations taken by the algorithm.
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
                    // Footer note for the results card.
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
