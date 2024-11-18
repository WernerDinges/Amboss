package org.dinges.amboss.examples

import org.dinges.amboss.math.data.bundle
import kotlin.math.pow

fun `Minimum with Gradient Descent`() {

    val f = { x: Float -> (x - 3).pow(2) }

    val learningRate = .1f
    val step = 1e-3f
    val iterations = 48

    val solution by bundle(iterations) {
        updateRule { _, _, x -> x - learningRate * (f(x+step/2f) - f(x-step/2f)) / step }
    }

    println(solution)
}

fun `5-Dimentional Minimun with Gradient Descent`() {

    // f(x) = Σ (x_i - i)^2
    val f = { x: List<Float> ->
        (x[0]-1).pow(2) + (x[1]-2).pow(2) + (x[2]-3).pow(2) + (x[3]-4).pow(2) + (x[4]-5).pow(2)
    }
    val vars = listOf("a", "b", "c", "d", "e")

    val learningRate = .1f
    val step = 1e-3f
    val iterations = 100

    val solution by bundle(iterations) {
        variables {
            for(name in vars)
                add(name, 0f)
        }
        updateRule { v, name, x ->
            val xp = List(5) { i -> v[name]!! + if(name == vars[i]) step/2f else 0f }
            val xm = List(5) { i -> v[name]!! - if(name == vars[i]) step/2f else 0f }

            x - learningRate * (f(xp) - f(xm)) / step
        }
    }

    println(solution)
}