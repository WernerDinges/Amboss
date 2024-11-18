package org.dinges.amboss.math.probabilityTheory

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextFloat

/**
 * Float mathematical function that gives the probabilities
 * of occurrence of possible outcomes for an experiment.
 */
interface FloatDistribution {
    fun sample() : Float
}

/**
 * Integer mathematical function that gives the probabilities
 * of occurrence of possible outcomes for an experiment.
 */
interface IntDistribution {
    fun sample(): Int
}

/**
 * Integer probability distribution with equal
 * probability density for any sample value.
 *
 * @param range Integer range of distribution.
 */
data class IntUniform(val range: IntRange): IntDistribution {
    override fun sample() = range.random()
}

/**
 * Float probability distribution with equal
 * probability density for any sample value.
 *
 * @param from Lower limit of distribution.
 * @param to Upper limit of distribution.
 */
data class FloatUniform(val from: Float, val to: Float): FloatDistribution {
    override fun sample(): Float = from + nextFloat() * (to - from)
}

/**
 * Normal probability distribution - with a peak in the center
 * and symmetrical sides. It is well suited for modeling
 * deviations in natural processes.
 *
 * @param mean Expectation, the point of the highest density.
 * @param deviation Standard deviation of the distribution.
 */
data class FloatGaussian(val mean: Float, val deviation: Float): FloatDistribution {
    override fun sample(): Float {
        // Box-Muller Transform to generate a standard Gaussian random variable
        val u1 = nextFloat()
        val u2 = nextFloat()
        val standardGaussian = sqrt(-2f * ln(u1)) * cos(2f * PI * u2).toFloat()
        // Scale and shift to match desired mean and standard deviation
        return mean + deviation * standardGaussian
    }
}

/**
 * Object for more convenient Integer sample picking.
 */
class IntDistributionState(private val distribution: IntDistribution) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Int {
        return distribution.sample()
    }
}

/**
 * Object for more convenient Float sample picking.
 */
class FloatDistributionState(private val distribution: FloatDistribution) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Float {
        return distribution.sample()
    }
}

/**
 * A delegated getter of a IntUniform.
 *
 * @param range Integer range of distribution.
 *
 * @sample org.dinges.amboss.examples.intUniformSample
 */
fun uniform(range: IntRange) = IntDistributionState(IntUniform(range))

/**
 * A delegated getter of a FloatUniform.
 *
 * @param from Lower limit of the distribution.
 * @param to Upper limit of the distribution.
 *
 * @sample org.dinges.amboss.examples.floatUniformSample
 */
fun uniform(from: Float, to: Float) = FloatDistributionState(FloatUniform(from, to))

/**
 * A delegated getter of a Gaussian distribution.
 *
 * @param mean Expectation, the point of the highest density.
 * @param deviation Standard deviation of the distribution.
 *
 * @sample org.dinges.amboss.examples.gaussianSample
 */
fun gaussian(mean: Float, deviation: Float) = FloatDistributionState(FloatGaussian(mean, deviation))