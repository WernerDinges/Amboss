package org.dinges.amboss.examples

import org.dinges.amboss.math.data.bundle
import org.dinges.amboss.math.probabilityTheory.gaussian
import kotlin.random.Random.Default.nextFloat

fun `Prisoner's dilemma`() {

    val generations = 10
    val populationSize = 200
    val survivors = 20

    // Average cooperation rate in a population.
    var average = 0.5f

    // Generate a population.
    fun coops(averageRate: Float) = mutableMapOf<String, Float>().apply {
        val mu by gaussian(averageRate, .1f)
        for(i in 0 ..< populationSize)
            this += "$i" to mu.crop()
    }
    var coop = coops(average)

    bundle(generations) {
        // Define a list of gains for the population (all 0 by default).
        variables {
            for(i in 0 ..< populationSize)
                add("$i")
        }
        // For each generation we rewrite the gains.
        updateRule { _, name, _ ->
            var fitness = 0f
            // Each unit plays the game with all the other units.
            for(i in (0 ..< populationSize).filter { it != name.toInt() })
                fitness += coop[name]!!.play(coop["$i"]!!)
            fitness
        }
        // At the end of each iteration, we choose most successful
        // survivors and average their cooperation rate.
        updateAllRule { fits ->
            val survived = fits.entries.sortedBy { it.value }
                .drop(populationSize - survivors).map {it.key}
            average = coop.filter { it.key in survived }.values.average().toFloat()
            // New generation for the next iteration.
            coop = coops(average)
        }

    }

    println(average)
}

private fun Float.crop() = this
    .coerceAtLeast(0f)
    .coerceAtMost(1f)

private fun Float.play(other: Float): Int {
    val coop1 = nextFloat() < this
    val coop2 = nextFloat() < other

    return when {
        coop1 && coop2 -> -10
        coop1 -> -20
        coop2 -> 0
        else -> -3
    }
}