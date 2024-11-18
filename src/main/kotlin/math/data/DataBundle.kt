package org.dinges.amboss.math.data

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A flat data set that is subsequently conferenced
 * into a list or a float number.
 */
class DataBundle<T> {

    private val collection = mutableListOf<T>()
    val list get() = collection.toList()

    /**
     * Write new data.
     */
    fun write(data: T) {
        collection.add(data)
    }

    /**
     * Write new data but fancier.
     */
    fun write(scope: BundleScope<T>.() -> Unit) {
        object : BundleScope<T> {
            var addCondition: ((T) -> Boolean)? = null

            override fun add(data: T) {
                if(addCondition?.invoke(data) != false)
                    write(data)
            }
            override fun addIf(condition: (T) -> Boolean) {
                addCondition = condition
            }
        }.apply(scope)
    }

}

/**
 * Data getter delegator.
 */
class DataBundleState<T>(val bundle: DataBundle<T>) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): List<T> {
        return bundle.list
    }
}

/**
 * DataBundle constructor.
 *
 * @param list Data set.
 *
 * @sample org.dinges.amboss.examples.bundleList
 */
fun <T> bundle(list: List<T>): DataBundleState<T> = bundle { list.forEach { add(it) } }

/**
 * DataBundle constructor but fancier.
 *
 * @param scope Space for specifying data itself and
 * its writing rule.
 *
 * @sample org.dinges.amboss.examples.bundleScope
 */
fun <T> bundle(scope: BundleScope<T>.() -> Unit): DataBundleState<T> {
    val bundle = DataBundle<T>()

    object : BundleScope<T> {
        var addCondition: ((T) -> Boolean)? = null

        override fun add(data: T) {
            if(addCondition?.invoke(data) != false)
                bundle.write(data)
        }
        override fun addIf(condition: (T) -> Boolean) {
            addCondition = condition
        }
    }.apply(scope)

    return DataBundleState(bundle)
}

/**
 * DataBundle for iterative data processing.
 *
 * @param iterations Number of traversal cycles for each element.
 * @param scope Space for specifying data and rules of iterations and writing.
 *
 * @sample org.dinges.amboss.examples.iterativeBundleScope
 */
fun bundle(iterations: Int, scope: IterativeBundleScope.() -> Unit): DataBundleState<Float> {
    val variables = mutableMapOf<String, Float>()
    var update: (variables: Map<String, Float>, String, Float) -> Float = { _, _, _ -> 0f }
    var updateAll: (Map<String, Float>) -> Unit = {  }
    var collapse: () -> Boolean = { false }

    object : IterativeBundleScope {

        override fun variables(scope: VariablesScope.() -> Unit) {
            object : VariablesScope {
                override fun add(name: String, default: Float) { variables += name to default }
            }.apply(scope)
        }

        override fun variables(vararg variable: String) {
            variable.forEach { variables += it to 0f }
        }

        override fun updateRule(rule: (variables: Map<String, Float>, name: String, x: Float) -> Float) {
            update = rule
        }

        override fun updateAllRule(rule: (Map<String, Float>) -> Unit) {
            updateAll = rule
        }

        override fun breakIf(rule: () -> Boolean) {
            collapse = rule
        }

    }.apply(scope)

    if(variables.isEmpty())
        variables += "" to 0f

    for(i in 0 until iterations) {
        for ((name, value) in variables)
            variables[name] = update(variables, name, value)

        updateAll(variables)

        if(collapse())
            break
    }

    return DataBundleState(DataBundle<Float>().apply {
        write {
            for((_, value) in variables)
                add(value)
        }
    })
}

/**
 * Scope for data manipulation.
 */
interface BundleScope<T> {
    /**
     * Add data to the data bundle.
     */
    fun add(data: T)

    /**
     * Condition for adding each item to the list.
     */
    fun addIf(condition: (T) -> Boolean)
}

/**
 * Scope for iterative data manipulation.
 */
interface IterativeBundleScope {
    /**
     * Variables definition.
     */
    fun variables(scope: VariablesScope.() -> Unit)

    /**
     * Variables definition.
     */
    fun variables(vararg variable: String)

    /**
     * Update of each variable's value at each iteration.
     *
     * @param rule Updated value taken from other variables,
     * updated variable's name and its current value.
     */
    fun updateRule(rule: (Map<String, Float>, String, Float) -> Float)

    /**
     * What happens in the end of each iteration.
     * Not intended to change the state of the data bundle.
     *
     * @param rule Lambda function that has immutable access
     * to the list of all the variables.
     */
    fun updateAllRule(rule: (Map<String, Float>) -> Unit)

    /**
     * Stop iterations early (sometimes you need to).
     */
    fun breakIf(rule: () -> Boolean)
}

/**
 * Scope for defining an iterative data bundle's variables.
 */
interface VariablesScope {
    fun add(name: String, default: Float = 0f)
}

/**
 * Similar to an ordinary average, except that instead of
 * each of the data points contributing equally to the final average,
 * some data points contribute more than others.
 *
 * @param element Returns element impact that is already weighted.
 * Has access to the element's index and its current value.
 */
fun <T: Number> DataBundleState<T>.weightedAverage(element: (Int, Float) -> Float): Float {
    return this.bundle.list.mapIndexed { i, num -> element(i, num.toFloat()) }.average().toFloat()
}

/**
 * Similar to an ordinary average, except that instead of
 * each of the data points contributing equally to the final average,
 * some data points contribute more than others.
 *
 * @param weights List of pre-conditioned weights for each element in
 * the data bundle.
 */
fun <T: Number> DataBundleState<T>.weightedAverage(weights: List<Float>): Float {
    require(this.bundle.list.size == weights.size) { "Weights list size and data bundle size must be equal!" }
    return this.bundle.list.mapIndexed { i, num -> num.toFloat() * weights[i] }.sum() / weights.sum()
}

/**
 * Expected value of the squared deviation from the mean of a random variable
 */
fun <T: Number> DataBundleState<T>.variance(): Float {
    val mean = this.bundle.list.map { it.toFloat() }.average().toFloat()
    return this.bundle.list.map { (it.toFloat() - mean).pow(2) }.sum() / this.bundle.list.size
}

/**
 * Disparity between an observed value of a variable and another
 * designated value, frequently the mean of that variable.
 */
fun <T: Number> DataBundleState<T>.deviation(): Float {
    return sqrt(variance())
}

/**
 * The "middle" number, when those numbers are listed
 * in order from smallest to greatest.
 */
fun <T: Number> DataBundleState<T>.median(): Float {
    val sorted = bundle.list.map { it.toFloat() }.sorted()
    val mid = bundle.list.size / 2
    return if (bundle.list.size % 2 == 0)
        (sorted[mid - 1] + sorted[mid]) / 2
    else
        sorted[mid]
}

/**
 * The element that is the 50% weighted percentile.
 *
 * @param weights List of pre-conditioned weights for each element in
 * the data bundle.
 */
fun <T: Number> DataBundleState<T>.medianWeighted(weights: List<Float>): Float {
    require(bundle.list.size == weights.size) { "Weights must have the same size as the data bundle." }

    val sortedPairs = bundle.list.map { it.toFloat() }.zip(weights).sortedBy { it.first }
    val cumulativeWeights = sortedPairs.runningFold(0f) { acc, pair -> acc + pair.second }
    val totalWeight = cumulativeWeights.last()
    val medianWeight = totalWeight / 2

    return sortedPairs.first { cumulativeWeights[sortedPairs.indexOf(it) + 1] >= medianWeight }.first
}

/**
 * Geometric mean. An average which indicates a central
 * tendency of a finite collection of positive real numbers
 * by using the product of their values
 * (as opposed to the arithmetic mean which uses their sum).
 */
fun <T: Number> DataBundleState<T>.gm(): Float {
    return if (bundle.list.isNotEmpty()) bundle.list.fold(1f) { acc, x -> acc * x.toFloat() }.pow(1f / bundle.list.size) else 0f
}

/**
 * Weighted geometric mean. A generalization of the geometric mean using
 * the weighted arithmetic mean.
 *
 * @param weights List of pre-conditioned weights for each element in
 * the data bundle.
 */
fun <T: Number> DataBundleState<T>.gmWeighted(weights: List<Float>): Float {
    require(bundle.list.size == weights.size) { "Weights must have the same size as the data bundle." }
    val weightedLogSum = bundle.list.zip(weights).map { (x, w) -> w * ln(x.toFloat()) }.sum()
    val totalWeight = weights.sum()
    return exp(weightedLogSum / totalWeight)
}

/**
 * Harmonic mean. It is the reciprocal of the arithmetic mean
 * of the reciprocals of the numbers.
 */
fun <T: Number> DataBundleState<T>.hm(): Float {
    return if (bundle.list.isNotEmpty()) bundle.list.size / bundle.list.map { 1f / it.toFloat() }.sum() else 0f
}

/**
 * Weighted harmonic mean. A generalization of the harmonic mean using weights
 * applied to each element.
 *
 * @param weights List of pre-conditioned weights for each element in
 * the data bundle.
 */
fun <T: Number> DataBundleState<T>.hmWeighted(weights: List<Float>): Float {
    require(bundle.list.size == weights.size) { "Weights must have the same size as the data bundle." }
    val weightedReciprocalSum = bundle.list.zip(weights).map { (x, w) -> w / x.toFloat() }.sum()
    val totalWeight = weights.sum()
    return totalWeight / weightedReciprocalSum
}

/**
 * Root Mean Square. It is the square root of the set's mean square.
 */
fun <T: Number> DataBundleState<T>.rms(): Float {
    return sqrt(bundle.list.map { it.toFloat().pow(2) }.sum() / bundle.list.size)
}