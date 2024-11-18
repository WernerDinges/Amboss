package org.dinges.amboss.examples

import org.dinges.amboss.math.data.bundle
import kotlin.math.abs
import kotlin.math.pow

fun `Bisection Method`() {

    val f = { x: Float -> x.pow(2) - 2 }

    val result by bundle(16) {
        variables {
            add("a", 0f)
            add("b", 2f)
        }
        updateRule { v, name, x ->
            val newX = (v["a"]!! + v["b"]!!) / 2f
            if(f(newX) * f(v[name]!!) < 0)
                x
            else
                newX
        }
    }

    println(result)
}

fun `Newton's Method`() {

    val f = { x: Float -> (x - 2).pow(2) }
    val step = 1e-3f
    var willBreak = false

    val result by bundle(16) {
        updateRule { _, _, x ->
            val fDeriv = (f(x + step/2f) - f(x - step/2f)) / step
            (x - f(x) / fDeriv).apply { willBreak = abs(this - x) < step }
        }
        breakIf { willBreak }
    }

    println(result)
}