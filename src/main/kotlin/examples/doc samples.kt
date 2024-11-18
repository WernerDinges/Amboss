package org.dinges.amboss.examples

import org.dinges.amboss.math.data.bundle
import org.dinges.amboss.math.probabilityTheory.gaussian
import org.dinges.amboss.math.probabilityTheory.uniform
import kotlin.math.pow

private fun intUniformSample() {
    val dice by uniform(1..6)
    // Each time a new value is generated.
    for(i in 1..100)
        println(dice)
}

private fun floatUniformSample() {
    val angle by uniform(0f, 360f)
    val radius by uniform(0f, 1f)
    val shots = 100
    // Each time new values are generated.
    for(i in 1..shots)
        println("The target is shot at a distance $radius from the center with an angle $angle")
}

private fun gaussianSample() {
    val distance by gaussian(0f, 1f)
    // Each time a new value is generated.
    for(i in 1..10)
        println("An accident occurred at a distance of $distance km from the city center")
}

private fun bundleList() {
    val data by bundle(listOf(0f, 1f, 2f))
}

private fun bundleScope() {
    val distance by gaussian(0f, 0.2f)
    val data by bundle {
        addIf { it in -1f..1f }
        for(i in 1..100)
            add(distance)
    }
}

private fun iterativeBundleScope() {
    val f = { x: Float -> x.pow(2) - 2 }
    // Bisection method
    val result by bundle(16) {
        variables {
            add("x1", 0f)
            add("x2", 2f)
        }
        updateRule { v, name, x ->
            val newX = (v["x1"]!! + v["x2"]!!) / 2f
            if(f(newX) * f(v[name]!!) < 0)
                x
            else
                newX
        }
    }
    // The root
    println(result)
}