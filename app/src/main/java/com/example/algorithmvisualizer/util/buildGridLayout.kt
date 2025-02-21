package com.example.algorithmvisualizer.util

import kotlin.math.cos
import kotlin.math.sin

/**
 * Genera un layout para nodos utilizando dos círculos concéntricos.
 *
 * Los nodos se dividen en dos grupos: el círculo externo (radio [outerRadius]) y el círculo interno (radio [innerRadius]).
 * Las posiciones se devuelven en coordenadas normalizadas ([0,1]), centradas en (0.5, 0.5).
 *
 * @param size Número de nodos.
 * @param outerRadius Radio del círculo externo (por ejemplo, 0.45).
 * @param innerRadius Radio del círculo interno (por ejemplo, 0.25).
 * @return Lista de posiciones como Pair<Float, Float>.
 */
fun buildConcentricCircleLayout(
    size: Int,
    outerRadius: Float = 0.45f,
    innerRadius: Float = 0.25f
): List<Pair<Float, Float>> {
    val positions = mutableListOf<Pair<Float, Float>>()
    // Determina cuántos nodos se asignan al círculo externo.
    val outerCount = if (size % 2 == 0) size / 2 else size / 2 + 1
    val innerCount = size - outerCount

    // Centro fijo en (0.5, 0.5)
    val centerX = 0.5f
    val centerY = 0.5f

    // Círculo externo: ángulos espaciados uniformemente.
    for (i in 0 until outerCount) {
        val angle = 2 * Math.PI * i / outerCount
        val x = centerX + outerRadius * cos(angle).toFloat()
        val y = centerY + outerRadius * sin(angle).toFloat()
        positions.add(x to y)
    }

    // Círculo interno: ángulos espaciados uniformemente con un offset para mejor distribución.
    for (i in 0 until innerCount) {
        val angle = 2 * Math.PI * i / innerCount + Math.PI / innerCount
        val x = centerX + innerRadius * cos(angle).toFloat()
        val y = centerY + innerRadius * sin(angle).toFloat()
        positions.add(x to y)
    }
    return positions
}