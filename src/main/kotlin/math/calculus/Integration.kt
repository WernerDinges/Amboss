package org.dinges.amboss.math.calculus

/**
 * Helper object to compare integration accuracy.
 */
object Integral {

    /**
     * Trapezoidal method integration.
     *
     * @param f Integrable function.
     * @param from Lower integration limit.
     * @param to Upper integration limit.
     * @param steps Number of segments.
     */
    fun trapezoidal(
        f: (Float) -> Float,
        from: Float, to: Float, steps: Int
    ): Float {
        require(to > from) { "The interval must be ascending." }

        val step = (to - from) / steps

        return trapez(f, from, step, steps)
    }

    /**
     * Quadratic polynomials method.
     *
     * @param f Integrable function.
     * @param from Lower integration limit.
     * @param to Upper integration limit.
     * @param steps Number of segments.
     */
    fun simpson(
        f: (Float) -> Float,
        from: Float, to: Float, steps: Int
    ): Float {
        require(steps > 0 && steps % 2 == 0) { "The number of intervals must be positive and even." }
        require(to > from) { "The interval must be ascending." }

        val step = (to - from) / steps

        return simps(f, from, to, step, steps)
    }

    private fun trapez(
        f: (Float) -> Float,
        from: Float, step: Float, n: Int
    ): Float {
        var sum = 0f

        for(i in 0 until n) {
            val a = from + (i * step)
            val b = a + step
            val h = (a + b) / 2

            sum += f(h) * step
        }

        return sum
    }

    private fun simps(
        f: (Float) -> Float,
        from: Float, to: Float, step: Float, n: Int
    ): Float {
        var result = f(from) + f(to)

        for (i in 1 until n) {
            val x = from + i * step
            result += if (i % 2 == 0) 2 * f(x) else 4 * f(x)
        }

        return (step / 3) * result
    }

}