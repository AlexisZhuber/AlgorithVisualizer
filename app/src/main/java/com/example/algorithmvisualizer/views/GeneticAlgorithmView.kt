package com.example.algorithmvisualizer.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.algorithm.fitnessFunction
import com.example.algorithmvisualizer.algorithm.generateGeneticAlgorithmSteps
import com.example.algorithmvisualizer.algorithm.Individual
import com.example.algorithmvisualizer.algorithm.GeneticAlgorithmStep
import com.example.algorithmvisualizer.ui.theme.BackgroundCard
import com.example.algorithmvisualizer.ui.theme.LightGray
import com.example.algorithmvisualizer.ui.theme.Primary
import com.example.algorithmvisualizer.ui.theme.TextColor
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * GeneticAlgorithmView displays a didactic visualization of a genetic algorithm.
 *
 * The UI is divided into:
 * 1. An explanation card.
 * 2. A row with two panels:
 *    - Left: Population on the fitness function curve.
 *    - Right: A histogram showing the distribution of individuals along the x-axis.
 * 3. A dynamic convergence panel that shows a progress bar and a text message indicating
 *    in simple terms the current stage of evolution, with the bar color changing according to the stage.
 *
 * Playback controls and a slider allow navigation through generations.
 */
@Composable
fun GeneticAlgorithmView() {
    val populationSize = 50
    val totalGenerations = 20

    // States for the genetic algorithm simulation.
    val steps = remember { mutableStateListOf<GeneticAlgorithmStep>() }
    var currentStepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }

    // Function to reset the simulation.
    fun resetSimulation() {
        // Generate initial population with random candidates.
        val initialPopulation = List(populationSize) {
            val x = Random.nextFloat() // random value in [0,1]
            Individual(x, fitnessFunction(x))
        }
        steps.clear()
        steps.addAll(generateGeneticAlgorithmSteps(initialPopulation, totalGenerations))
        currentStepIndex = 0
        isPlaying = false
    }

    // Initialize simulation.
    LaunchedEffect(Unit) { resetSimulation() }
    // Auto-advance generations if in play mode.
    LaunchedEffect(isPlaying, currentStepIndex) {
        if (isPlaying && currentStepIndex < steps.lastIndex) {
            delay(100L)
            currentStepIndex++
        } else if (isPlaying) {
            isPlaying = false
        }
    }
    val currentStep = steps.getOrNull(currentStepIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Explanation Card.
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = BackgroundCard)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.explanation_text_ga),
                    color = TextColor,
                    // Use a larger font size with increased line height for readability.
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    textAlign = TextAlign.Justify
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Row with two panels.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Panel 2A: Population on Fitness Function.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(300.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.ga_population_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(width = 2.dp, color = MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val marginPx = with(LocalDensity.current) { 16.dp.toPx() }
                        val canvasWidth = constraints.maxWidth.toFloat()
                        val canvasHeight = constraints.maxHeight.toFloat()
                        val effectiveWidth = canvasWidth - 2 * marginPx
                        val effectiveHeight = canvasHeight - 2 * marginPx
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Background grid.
                            val gridColor = Color.LightGray.copy(alpha = 0.5f)
                            val numHorizontalLines = 5
                            val numVerticalLines = 5
                            for (i in 0..numHorizontalLines) {
                                val y = marginPx + i * effectiveHeight / numHorizontalLines
                                drawLine(
                                    color = gridColor,
                                    start = Offset(marginPx, y),
                                    end = Offset(marginPx + effectiveWidth, y),
                                    strokeWidth = 1f
                                )
                            }
                            for (j in 0..numVerticalLines) {
                                val x = marginPx + j * effectiveWidth / numVerticalLines
                                drawLine(
                                    color = gridColor,
                                    start = Offset(x, marginPx),
                                    end = Offset(x, marginPx + effectiveHeight),
                                    strokeWidth = 1f
                                )
                            }
                            // Axes.
                            drawLine(
                                color = Color.DarkGray,
                                start = Offset(marginPx, marginPx + effectiveHeight),
                                end = Offset(marginPx + effectiveWidth, marginPx + effectiveHeight),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Color.DarkGray,
                                start = Offset(marginPx, marginPx),
                                end = Offset(marginPx, marginPx + effectiveHeight),
                                strokeWidth = 2f
                            )
                            // Axis labels.
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.DKGRAY
                                    textSize = 30f
                                    textAlign = android.graphics.Paint.Align.LEFT
                                }
                                drawText("x", marginPx + effectiveWidth - 30, marginPx + effectiveHeight - 10, paint)
                                drawText("f(x)", marginPx + 10, marginPx + 30, paint)
                            }
                            // Draw fitness function curve.
                            val numPoints = 100
                            val path = Path().apply {
                                for (i in 0 until numPoints) {
                                    val xNorm = i / (numPoints - 1).toFloat()
                                    val yValue = fitnessFunction(xNorm)
                                    val xCanvas = marginPx + xNorm * effectiveWidth
                                    val yCanvas = marginPx + (1 - yValue) * effectiveHeight
                                    if (i == 0) moveTo(xCanvas, yCanvas) else lineTo(xCanvas, yCanvas)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color.Blue,
                                style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            // Mark the optimum point.
                            val optimumX = marginPx + 0.5f * effectiveWidth
                            val optimumY = marginPx + (1 - 1f) * effectiveHeight
                            drawCircle(
                                color = Color.Magenta,
                                center = Offset(optimumX, optimumY),
                                radius = 12f
                            )
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.MAGENTA
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                drawText("Optimum", optimumX, optimumY - 16, paint)
                            }
                            // Draw individuals.
                            currentStep?.population?.forEach { individual ->
                                val xPos = marginPx + individual.x * effectiveWidth
                                val yPos = marginPx + (1 - individual.fitness) * effectiveHeight
                                // Shadow for glow effect.
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    center = Offset(xPos + 2, yPos + 2),
                                    radius = 10f
                                )
                                val bestIndividual = currentStep.population.maxByOrNull { it.fitness }
                                val circleColor = if (bestIndividual != null && bestIndividual == individual) Color.Green else Color.Red
                                drawCircle(
                                    color = circleColor,
                                    center = Offset(xPos, yPos),
                                    radius = 10f
                                )
                            }
                        }
                    }
                }
            }
            // Panel 2B: Histogram of Individuals' x Distribution.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(300.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.ga_distribution_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(width = 2.dp, color = MaterialTheme.colorScheme.outline),
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val marginPx = with(LocalDensity.current) { 16.dp.toPx() }
                        val canvasWidth = constraints.maxWidth.toFloat()
                        val canvasHeight = constraints.maxHeight.toFloat()
                        val effectiveWidth = canvasWidth - 2 * marginPx
                        val effectiveHeight = canvasHeight - 2 * marginPx
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val binCount = 10
                            val binWidth = effectiveWidth / binCount
                            val bins = IntArray(binCount) { 0 }
                            currentStep?.population?.forEach { individual ->
                                val bin = (individual.x * binCount).toInt().coerceAtMost(binCount - 1)
                                bins[bin]++
                            }
                            val maxCount = bins.maxOrNull() ?: 1
                            bins.forEachIndexed { index, count ->
                                val x = marginPx + index * binWidth
                                val barHeight = (count.toFloat() / maxCount) * effectiveHeight
                                drawRect(
                                    color = Color.Cyan,
                                    topLeft = Offset(x, marginPx + effectiveHeight - barHeight),
                                    size = Size(binWidth * 0.8f, barHeight)
                                )
                            }
                            drawLine(
                                color = Color.DarkGray,
                                start = Offset(marginPx, marginPx + effectiveHeight),
                                end = Offset(marginPx + effectiveWidth, marginPx + effectiveHeight),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Convergence Panel: Dynamic Explanation with Color Bar.
        Text(
            text = stringResource(id = R.string.convergence_progress),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(width = 2.dp, color = MaterialTheme.colorScheme.outline),
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val marginPx = with(LocalDensity.current) { 16.dp.toPx() }
                val canvasWidth = constraints.maxWidth.toFloat()
                val canvasHeight = constraints.maxHeight.toFloat()
                val effectiveWidth = canvasWidth - 2 * marginPx
                val effectiveHeight = canvasHeight - 2 * marginPx

                // Draw a background grid.
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = Color.LightGray.copy(alpha = 0.5f)
                    val numHorizontalLines = 4
                    for (i in 0..numHorizontalLines) {
                        val y = marginPx + i * effectiveHeight / numHorizontalLines
                        drawLine(
                            color = gridColor,
                            start = Offset(marginPx, y),
                            end = Offset(marginPx + effectiveWidth, y),
                            strokeWidth = 1f
                        )
                    }
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(marginPx, marginPx),
                        end = Offset(marginPx, marginPx + effectiveHeight),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.DarkGray,
                        start = Offset(marginPx, marginPx + effectiveHeight),
                        end = Offset(marginPx + effectiveWidth, marginPx + effectiveHeight),
                        strokeWidth = 2f
                    )
                    // Plot best fitness evolution.
                    if (steps.isNotEmpty()) {
                        val path = Path().apply {
                            steps.forEachIndexed { index, step ->
                                val x = marginPx + (index.toFloat() / (steps.size - 1)) * effectiveWidth
                                val y = marginPx + (1 - step.bestFitness) * effectiveHeight
                                if (index == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color.Magenta,
                            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        steps.forEachIndexed { index, step ->
                            val x = marginPx + (index.toFloat() / (steps.size - 1)) * effectiveWidth
                            val y = marginPx + (1 - step.bestFitness) * effectiveHeight
                            drawCircle(
                                color = Color.Magenta,
                                center = Offset(x, y),
                                radius = 5f
                            )
                        }
                    }
                }
                // Dynamic text and color bar.
                val generation = currentStep?.generation ?: 0
                val (barColor, dynamicText) = when {
                    generation == 0 -> Pair(Color.LightGray, stringResource(id = R.string.ga_stage_start))
                    generation in 1..5 -> Pair(Color.Yellow, stringResource(id = R.string.ga_stage_exploring))
                    generation in 6..10 -> Pair(Color.Cyan, stringResource(id = R.string.ga_stage_improving))
                    generation > 10 -> Pair(Color.Green, stringResource(id = R.string.ga_stage_converging))
                    else -> Pair(Color.White, "")
                }
                // Draw a semi-transparent colored rectangle at the top of the panel.
                Canvas(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                    drawRect(
                        color = barColor.copy(alpha = 0.5f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height)
                    )
                }
                // Display the dynamic text below the bar.
                Text(
                    text = dynamicText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display current generation and best fitness.
        currentStep?.let { step ->
            Text(
                text = stringResource(id = R.string.generation_label, step.generation),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.best_fitness_label, step.bestFitness),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Slider for generation control.
        if (steps.isNotEmpty()) {
            Slider(
                value = currentStepIndex.toFloat(),
                onValueChange = { newValue ->
                    currentStepIndex = newValue.toInt().coerceIn(0, steps.lastIndex)
                    isPlaying = false
                },
                valueRange = 0f..(steps.size - 1).toFloat(),
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = {
                currentStepIndex = (currentStepIndex - 1).coerceAtLeast(0)
                isPlaying = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_previous),
                    contentDescription = stringResource(id = R.string.previous),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = {
                currentStepIndex = (currentStepIndex + 1).coerceAtMost(steps.lastIndex)
                isPlaying = false
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.skip_next),
                    contentDescription = stringResource(id = R.string.next),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { resetSimulation() }) {
                Icon(
                    painter = painterResource(id = R.drawable.refresh),
                    contentDescription = stringResource(id = R.string.reset),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
