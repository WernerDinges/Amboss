# Amboss

**Amboss** (German for "Anvil") is a modest attempt to develop a DSL (Domain-Specific Language) for computational statistics.
The project is still in the draft stage, but its tools are already showing intermediate results.

The project focuses on providing a comfortable and flexible toolkit applicable to a wide variety of tasks using as few methods and functions as possible.
At the moment it is possible to implement solutions such as [Monte Carlo method](https://en.wikipedia.org/wiki/Monte_Carlo_method), [evolutionary algorithms](https://en.wikipedia.org/wiki/Evolutionary_algorithm), [optimization](https://en.wikipedia.org/wiki/Mathematical_optimization) for any number of parameters
and basic statistics functions - [median number, weighted parameters, etc.](https://en.wikipedia.org/wiki/Mean)

Table of Contents:
- [DataBundle](#databundle)
- [Iterative algorithms](#iterative_algorithms)
- [Evolutionary algorithm and Game Theory](#evolutionary_algorithm_and_game_theory)
- [All the examples](#all_the_examples)

## DataBundle

DataBundle is a fundamental concept, which is a revised Array/List concept.
The main feature of data bundles is an extended setup process including function blocks, support for iterations, internal variables, etc.
```kotlin
val x by uniform(0f, 1f)
val y by uniform(0f, 1f)

val data by bundle<Float> {
    addIf { it < 1f }
    repeat(1_000_000) {
        add(hypot(x, y))
    }
}
// Calculated value of PI
println(4f * data.size / n.toFloat())
```
[Source](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Monte%20Carlo%20Simulations.kt)

In this short example, we first define the variables X and Y (samples are generated each time the variable is called, as if we were calling a function to generate a random value between 0f and 1f.
There are also an option for normal distribution:
```kotlin
// Average value is 1 with expected deviation of 0.5.
val mu by gaussian(1f, .5f)
```
Next, we declare a data bundle filled with all radius vector values of random points that fall within a circle segment with radius 1:
```kotlin
bundle<Float> {
    addIf { it < 1f }
    repeat(1_000_000) { add(hypot(x, y)) }
}
```
This higher function returns a `DataBundleState<T>` object, but when delegated using the `by` keyword, the function returns just a `List<T>` object.

By calculating the size of the list, we can derive an approximation of π.

## Iterative algorithms

It is also possible to implement iterative processes with the help of data-bundles.
In this case, each element of the collection is called a 'variable', and the function block requires specifying the rules for updating each such variable.
If desired, it is possible to specify the rules for interrupting the remaining iterations.
```kotlin
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
```
[Source](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Finding%20Root.kt)

In this example, we implement the bisection method to find one of the roots of the equation within the limits of A and B.
It definitely isn't the most efficient implementation of the method, but the goal is not performance,
but the flexibility of implementation with a balanced level of abstraction.

The output of this example is a list of two numbers that are almost identical to each other -
either of them can be taken as an approximation of the found root. In this case, 10-20 iterations are enough
for a good approximation.

## Evolutionary algorithm and Game Theory

The main thing for which this project was created is my personal interest in modeling evolutionary processes and Game Theory simulations.

Here we will write simple code to simulate the Prisoner's Dilemma ([full code here](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Game%20Theory.kt)).
First let's define the conditions: there will be 10 generations in total (no more needed), in each generation the population will have 200 units (no more needed),
in each generation the 20 most successful units will survive.
```kotlin
val generations = 10
val populationSize = 200
val survivors = 20
```
Initially, the average cooperation rate for the population will be 0.5f.
```kotlin
var average = 0.5f
```
The function that will generate each new population is as follows:
```kotlin
fun coops() = mutableMapOf<String, Float>().apply {
    val mu by gaussian(average, .1f)
    for(i in 0 ..< populationSize)
        this += "$i" to mu.crop()
}
```
Each unit has its own mutated cooperation rate, on average deviating slightly from the average by 0.1f.

Let's set the primary population:
```kotlin
var coop = coops()
```
Then inside the block of the `bundle { ... }` function we build all variables.
```kotlin
bundle(generations) {
    variables {
        for(i in 0 ..< populationSize)
            add("$i")
    }
    // ...
}
```
Each variable will be an indicator of how successful a unit is in the 'game'.

Each generation, a unit must 'play' with all other units and receive a number of points that is representative of the unit's success in the population.
```kotlin
// ...
updateRule { _, name, _ ->
    var fitness = 0f
    for(i in (0 ..< populationSize).filter { it != name.toInt() })
        fitness += coop[name]!!.play(coop["$i"]!!)
    fitness
}
// ...
```
At the end of each iteration, we select the surviving units and average out the cooperation rate to create a new population with mutations based on it.
```kotlin
updateAllRule { fits ->
    val survived = fits
        .entries
        .sortedBy { it.value }
        .drop(populationSize - survivors)
        .map {it.key}
    average = coop
        .filter { it.key in survived }
        .values
        .average()
        .toFloat()
    coop = coops()
}
```
After all iterations, we can see that already after a few generations, the overall cooperation rate is rapidly approaching 0.
This is the correct answer predicted by Game Theory.

# All the examples
- [Bisection method](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Finding%20Root.kt)
- [Newton's method](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Finding%20Root.kt)
- [Prisoner's dilemma](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Game%20Theory.kt)
- [Find pi](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Monte%20Carlo%20Simulations.kt)
- [1D integral with Monte-Carlo method](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Monte%20Carlo%20Simulations.kt)
- [1-dimensional minimum with gradient descent](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Optimization.kt)
- [15-dimensional minimum with gradient descent](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Optimization.kt)
- [Spline (slightly off topic)](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Spline.kt)
- [Deviation, Median, Weighted median, Hermonic mean, etc.](https://github.com/WernerDinges/Amboss/blob/master/src/main/kotlin/examples/Statistics.kt)
