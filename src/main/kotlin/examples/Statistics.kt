package org.dinges.amboss.examples

import org.dinges.amboss.math.data.*

fun `DataBundle Features`() {

    val data = bundle(listOf(1, 2, 3, 4, 5, 6))
    val weights = listOf(0f, 1f, 1f, 0f, 0f, 0f)

    println(data.weightedAverage { _, x -> x * 2f })
    println(data.weightedAverage(weights))
    println()

    println(data.variance())
    println()

    println(data.deviation())
    println()

    println(data.median())
    println(data.medianWeighted(weights))
    println()

    println(data.gm())
    println(data.gmWeighted(weights))
    println()

    println(data.hm())
    println(data.hmWeighted(weights))
    println()

    println(data.rms())

}