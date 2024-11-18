package org.dinges.amboss.examples

import org.dinges.amboss.math.calculus.Integral
import org.dinges.amboss.math.data.bundle
import org.dinges.amboss.math.probabilityTheory.uniform
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.system.measureTimeMillis

fun `Find pi`() {

    val n = 10_000_000

    val x by uniform(0f, 1f)
    val y by uniform(0f, 1f)

    // In this scope we collect data
    val data by bundle<Float> {
        // Set a condition of adding new data to the bundle
        addIf { it < 1f }
        repeat(n) {
            // Delegated variables generate samples
            // from the uniform distribution.
            add(hypot(x, y))
        }
    }

    println(4f * data.size / n.toFloat())
}

fun `1D Integral`() {

    val f = { x: Float -> sin(x) }
    val from = 0f
    val to = PI.toFloat()
    val steps = 1000

    val x by uniform(from, to)
    val y by uniform(0f, 1f)
    val n = 1_000_000

    // Trapezoidal method
    var trS = 0f
    val trTime = measureTimeMillis {
        trS = Integral.trapezoidal(f, from, to, steps)
    }
    println("Trapezoidal: $trS")
    println(" | $trTime ms")

    // Simpson's method
    var siS = 0f
    val siTime = measureTimeMillis {
        siS = Integral.simpson(f, from, to, steps)
    }
    println("Simpson: $siS")
    println(" | $siTime ms")

    // Monte-Carlo's method
    var mcS = 0f
    val mcTime = measureTimeMillis {
        val dots by bundle<Float> {
            addIf { it < 1f }
            repeat(n) { add(y / f(x)) }
        }
        mcS = dots.size / n.toFloat() * (to - from)
    }
    println("Monte-Carlo: $mcS")
    println(" | $mcTime ms")

}