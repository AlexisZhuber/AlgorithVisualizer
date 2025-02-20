package com.example.algorithmvisualizer.algorithm

import kotlin.random.Random

/**
 * Data class representing a step in the Bubble Sort process.
 *
 * @param array A snapshot of the list at that moment.
 * @param comparedIndices The pair of indices that are being compared in this step.
 */
data class BubbleSortStep(
    val array: List<Int>,
    val comparedIndices: Pair<Int, Int>
)

/**
 * Generates a list of BubbleSortStep objects that describe each comparison and swap
 * during the Bubble Sort process. This allows us to "replay" the process step by step.
 *
 * @param initialArray The initial list of numbers.
 * @return A list of steps describing the sorting process.
 */
fun generateBubbleSortSteps(initialArray: List<Int>): List<BubbleSortStep> {
    val steps = mutableListOf<BubbleSortStep>()
    val array = initialArray.toMutableList()
    val n = array.size
    for (i in 0 until n) {
        var swapped = false
        for (j in 0 until n - i - 1) {
            // Record the state before a possible swap
            steps.add(BubbleSortStep(array = array.toList(), comparedIndices = Pair(j, j + 1)))
            if (array[j] > array[j + 1]) {
                val temp = array[j]
                array[j] = array[j + 1]
                array[j + 1] = temp
                swapped = true
            }
        }
        if (!swapped) break
    }
    // Final step to show the sorted list
    steps.add(BubbleSortStep(array = array.toList(), comparedIndices = Pair(-1, -1)))
    return steps
}

/**
 * Resets the list of numbers with random values.
 *
 * @param arrayState The mutable list that will contain the new values.
 * @param size The number of elements to generate.
 */
fun resetArray(
    arrayState: MutableList<Int>,
    size: Int,
    maxNumber: Int = 100
) {
    arrayState.clear()
    repeat(size) {
        // Generate random values between 0..maxNumber
        arrayState.add(Random.nextInt(0, maxNumber + 1))
    }
}


