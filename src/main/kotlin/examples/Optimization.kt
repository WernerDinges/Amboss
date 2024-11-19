package org.dinges.amboss.examples

import org.dinges.amboss.math.data.bundle
import kotlin.math.pow

fun `Minimum with Gradient Descent`() {

    val f = { x: Float -> (x - 3).pow(2) }

    val learningRate = .1f
    val step = 1e-3f

    val solution by bundle(48) {
        updateRule { _, _, x -> x - learningRate * (f(x+step/2f) - f(x-step/2f)) / step }
    }

    println(solution)
}

fun `15-Dimensional Minimun with Gradient Descent`() {

    // f(x) = Σ (x_i - i)^2
    val f = { x: List<Float> ->
        var sum = 0f
        for(i in x.indices)
            sum += (x[i] - (i+1)).pow(2)
        sum
    }
    // Dimensions
    val vars = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "o", "p")

    val learningRate = .1f
    val step = 1e-3f

    val solution by bundle(50) {
        variables { for(name in vars) add(name) }
        updateRule { v, name, x ->
            val xp = List(vars.size) { i -> v[name]!! + if(name == vars[i]) step/2f else 0f }
            val xm = List(vars.size) { i -> v[name]!! - if(name == vars[i]) step/2f else 0f }

            x - learningRate * (f(xp) - f(xm)) / step
        }
    }

    println(solution)
}