package org.dinges.amboss.math.calculus

import org.dinges.amboss.math.fundamental.Point
import kotlin.math.pow

/**
 * Spline interpolator specification in a more boring form.
 *
 * @param point List of points
 */
fun spline(vararg point: Pair<Float, Float>): CubicSpline {
    require(point.size > 1) { "At least two points are required." }

    val points = mutableListOf<Point>()

    for((x, y) in point)
        points += Point(x, y)

    return CubicSpline(points)
}

/**
 * Spline interpolator specification in a more expressive form.
 *
 * @param scope Space for specifying all the points.
 */
fun spline(scope: SplineScope.() -> Unit): CubicSpline {
    val points = mutableListOf<Point>()

    object : SplineScope {
        override fun point(x: Float, y: Float) {
            points += Point(x, y)
        }
    }.apply(scope)

    require(points.size > 1) { "At least two points are required." }

    return CubicSpline(points)
}

/**
 * Scope for specifying spline points.
 */
interface SplineScope {
    fun point(x: Float, y: Float)
}

/**
 * Cubic interpolation.
 *
 * @param points The initial list of points.
 */
class CubicSpline(points: List<Point>) {
    private val x = points.map { it.x }.toFloatArray()
    private val y = points.map { it.y }.toFloatArray()
    private val n = points.size - 1

    private val a = y.copyOf()             // Coefficients a_i = y_i
    private val b = FloatArray(n)          // Coefficients b_i
    private val c = FloatArray(n + 1) // Coefficients c_i
    private val d = FloatArray(n)          // Coefficients d_i

    init {
        require(points.size > 1) { "At least two points are required." }

        computeCoefficients()
    }

    private fun computeCoefficients() {
        val h = FloatArray(n) { i -> x[i + 1] - x[i] }
        val alpha = FloatArray(n) { i ->
            if(i == 0) 0f else (3 / h[i] * (a[i + 1] - a[i]) - 3 / h[i - 1] * (a[i] - a[i - 1]))
        }

        // Solve the tridiagonal matrix for c
        val l = FloatArray(n + 1) { 1f }
        val mu = FloatArray(n + 1)
        val z = FloatArray(n + 1)

        for(i in 1 until n) {
            l[i] = 2 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1]
            mu[i] = h[i] / l[i]
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i]
        }

        for(j in n - 1 downTo 0) {
            c[j] = z[j] - mu[j] * c[j + 1]
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3
            d[j] = (c[j + 1] - c[j]) / (3 * h[j])
        }
    }

    /**
     * Calculate a missing point with a cubic polynomial.
     *
     * @param valueX X coordinate of the point we're looking for.
     */
    fun interpolate(valueX: Float): Float {
        require(valueX in x.first()..x.last()) { "xValue is out of bounds." }

        // Find the correct segment
        val i = x.indexOfLast { it <= valueX }
        val dx = valueX - x[i]

        // Evaluate the spline polynomial
        return a[i] + b[i] * dx + c[i] * dx.pow(2) + d[i] * dx.pow(3)
    }

}