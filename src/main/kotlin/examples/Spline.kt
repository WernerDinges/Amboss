package org.dinges.amboss.examples

import org.dinges.amboss.math.calculus.spline
import kotlin.math.PI
import kotlin.math.sin

fun `Spline interpolation`() {

    val f = { x: Float -> sin(x) }
    val segments = 5

    val foo = spline {
        for(i in 0..segments) {
            val x = i * PI.toFloat() / segments
            point(x, f(x))
        }
    }

    println(foo.interpolate(1.1f))
    println(f(1.1f))

}