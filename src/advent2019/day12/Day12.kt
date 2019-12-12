package advent2019.day12

import advent2019.logWithTime
import advent2019.readAllLines
import kotlin.math.absoluteValue
import kotlin.math.sign

val regex = Regex("<x=(-?)(\\d+),\\s*y=(-?)(\\d+),\\s*z=(-?)(\\d+)\\s*>")

data class Vector(val x: Int, val y: Int, val z: Int) {
    val absoluteValue get() = x.absoluteValue + y.absoluteValue + z.absoluteValue
    val sign get() = Vector(this.x.sign, this.y.sign, this.z.sign)
    operator fun minus(that: Vector): Vector = Vector(this.x - that.x, this.y - that.y, this.z - that.z)
    operator fun plus(that: Vector): Vector = Vector(this.x + that.x, this.y + that.y, this.z + that.z)
}

data class State(val locations: List<Vector>, val velocities: List<Vector>)

val State.energy get() = locations.indices.map { locations[it].absoluteValue * velocities[it].absoluteValue }.sum()

fun main() {
    val input = readAllLines("input-2019-12.txt")
//    val input = "<x=-1, y=0, z=2>\n<x=2, y=-10, z=-7>\n<x=4, y=-8, z=8>\n<x=3, y=5, z=-1>".lines()

    // phase 1
    var state = initialState(input)
    printState(0, state)
    (1..1000).forEach {
        state = step(state)
    }
    printState(1000, state)

    // phase 2 stupid and not really working yet...
    state = initialState(input)
    var iteration = 0
    val cache= mutableMapOf<State, Int>()

    while (!cache.containsKey(state)) {
        if (iteration % 100000 == 0) cache[state] = iteration
        state = step(state)
        iteration++
        if (iteration % 1000 == 0)print(".")
        if (iteration % 100000 == 0) println(iteration)
    }
    println()

    logWithTime("state at $iteration was encountered at ${cache[state]}")
    printState(iteration, state)


}

private fun printState(step: Int, state: State) {
    logWithTime("After $step steps:")
    state.locations.indices.forEach { i ->
        println("pos=${state.locations[i]} (${state.locations[i].absoluteValue}) vel=${state.velocities[i]} (${state.velocities[i].absoluteValue})")
    }
    println("energy is ${state.energy}")
}

fun initialState(input: List<String>): State {
    val locations = input
        .map { regex.matchEntire(it) ?: error("not matching '$it'") }
        .map { it.destructured }
        .map {
            Vector(
                it.component2().toInt() * if (it.component1() == "-") -1 else 1,
                it.component4().toInt() * if (it.component3() == "-") -1 else 1,
                it.component6().toInt() * if (it.component5() == "-") -1 else 1
            )
        }

    val velocities = (locations.indices).map { Vector(0, 0, 0) }

    return State(locations, velocities)
}

fun step(state: State): State {
    // gravity
    val newV = state.velocities.mapIndexed { index, velocity ->
        val changes = state.locations
            .map { it - state.locations[index] } // distances
            .map { it.sign } // directions
        val v = changes.fold(velocity, { acc, change -> acc + change })
        v
    }

    // move
    val newL = state.locations.indices.map { state.locations[it] + newV[it] }

    return State(newL, newV)
}

