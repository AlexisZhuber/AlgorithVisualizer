package com.example.algorithmvisualizer.algorithm

/**
 * Data class representing a step in the Selection Sort process.
 *
 * @param array A snapshot of the list at that moment.
 * @param comparedIndices The pair of indices that are currently being compared in this step.
 */
data class SelectionSortStep(
    val array: List<Int>,
    val comparedIndices: Pair<Int, Int>
)

/**
 * Generates a list of SelectionSortStep objects describing each comparison and swap
 * during the Selection Sort process. This lets us "replay" the process step by step.
 *
 * Selection Sort works by finding the smallest element in the unsorted portion of the array
 * and swapping it with the element at the start of that unsorted portion.
 *
 * @param initialArray The initial list of numbers.
 * @return A list of steps describing the Selection Sort process.
 */
fun generateSelectionSortSteps(initialArray: List<Int>): List<SelectionSortStep> {
    val steps = mutableListOf<SelectionSortStep>()
    val arr = initialArray.toMutableList()

    // Outer loop: move the boundary of the sorted portion from left to right.
    for (i in 0 until arr.size - 1) {
        var minIndex = i
        // Inner loop: find the minimum in the unsorted portion [i+1..end].
        for (j in i + 1 until arr.size) {
            // Record the step before checking or swapping.
            steps.add(SelectionSortStep(array = arr.toList(), comparedIndices = Pair(minIndex, j)))

            // If we find a smaller element, update minIndex.
            if (arr[j] < arr[minIndex]) {
                minIndex = j
            }
        }
        // After finding the smallest in the unsorted portion, swap it with arr[i].
        if (minIndex != i) {
            val temp = arr[i]
            arr[i] = arr[minIndex]
            arr[minIndex] = temp
        }
    }
    // Final step to show the fully sorted list.
    steps.add(SelectionSortStep(arr.toList(), Pair(-1, -1)))
    return steps
}
