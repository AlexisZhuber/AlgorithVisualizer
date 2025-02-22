package com.example.algorithmvisualizer.algorithm

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Represents an individual in the genetic algorithm.
 *
 * @property x The candidate solution (normalized between 0 and 1).
 * @property fitness The fitness value computed using the fitnessFunction.
 */
data class Individual(val x: Float, val fitness: Float)

/**
 * Represents a single generation step in the genetic algorithm visualization.
 *
 * @property generation The generation number.
 * @property population The list of individuals in this generation.
 * @property bestFitness The highest fitness value in the current population.
 */
data class GeneticAlgorithmStep(
    val generation: Int,
    val population: List<Individual>,
    val bestFitness: Float
)

/**
 * Fitness function: f(x) = 4 * x * (1 - x).
 *
 * This function is defined on the interval [0, 1] and has a maximum at x = 0.5.
 *
 * @param x A candidate solution value between 0 and 1.
 * @return The fitness value for x.
 */
fun fitnessFunction(x: Float): Float = 4 * x * (1 - x)

/**
 * Generates a list of steps simulating a genetic algorithm.
 *
 * The algorithm uses a simple selection (taking the top 50%),
 * crossover (average of two parents), and mutation (small random change) strategy.
 *
 * @param initialPopulation The initial list of individuals.
 * @param generations The number of generations to simulate.
 * @return A list of GeneticAlgorithmStep objects representing each generation.
 */
fun generateGeneticAlgorithmSteps(
    initialPopulation: List<Individual>,
    generations: Int
): List<GeneticAlgorithmStep> {
    val steps = mutableListOf<GeneticAlgorithmStep>()
    var currentPopulation = initialPopulation.map { it.copy() }
    steps.add(
        GeneticAlgorithmStep(
            generation = 0,
            population = currentPopulation,
            bestFitness = currentPopulation.maxOf { it.fitness }
        )
    )

    for (gen in 1..generations) {
        // Selection: sort by fitness descending and take top 50%
        val sortedPopulation = currentPopulation.sortedByDescending { it.fitness }
        val parents = sortedPopulation.take(max(1, sortedPopulation.size / 2))
        val newPopulation = mutableListOf<Individual>()
        // Reproduction: create new individuals using crossover and mutation.
        while (newPopulation.size < currentPopulation.size) {
            // Randomly select two parents.
            val parent1 = parents.random()
            val parent2 = parents.random()
            // Crossover: child x is the average of the parent's x values.
            var childX = (parent1.x + parent2.x) / 2f
            // Mutation: add a small random delta with a 30% chance.
            if (Random.nextFloat() < 0.3f) {
                childX += Random.nextFloat() * 0.1f - 0.05f // mutation in [-0.05, 0.05]
            }
            // Clamp the value between 0 and 1.
            childX = min(1f, max(0f, childX))
            val childFitness = fitnessFunction(childX)
            newPopulation.add(Individual(childX, childFitness))
        }
        currentPopulation = newPopulation
        steps.add(
            GeneticAlgorithmStep(
                generation = gen,
                population = currentPopulation,
                bestFitness = currentPopulation.maxOf { it.fitness }
            )
        )
    }
    return steps
}
