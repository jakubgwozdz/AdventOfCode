package advent2019.day12

import advent2019.logWithTime
import advent2019.readAllLines
import kotlin.math.absoluteValue
import kotlin.math.sign

val regex = Regex("<x=(-?)(\\d+),\\s*y=(-?)(\\d+),\\s*z=(-?)(\\d+)\\s*>")

interface Vector<V> {
    val absoluteValue: Int
    val sign: V
    operator fun minus(that: V): V
    operator fun plus(that: V): V
}

data class Vector3(val x: Int, val y: Int, val z: Int) : Vector<Vector3> {
    override val absoluteValue get() = x.absoluteValue + y.absoluteValue + z.absoluteValue
    override val sign get() = Vector3(this.x.sign, this.y.sign, this.z.sign)
    override operator fun minus(that: Vector3): Vector3 = Vector3(this.x - that.x, this.y - that.y, this.z - that.z)
    override operator fun plus(that: Vector3): Vector3 = Vector3(this.x + that.x, this.y + that.y, this.z + that.z)
}

data class Vector1(val x: Int) : Vector<Vector1> {
    override val absoluteValue get() = x.absoluteValue
    override val sign get() = Vector1(this.x.sign)
    override operator fun minus(that: Vector1): Vector1 = Vector1(this.x - that.x)
    override operator fun plus(that: Vector1): Vector1 = Vector1(this.x + that.x)
}

data class State<V : Vector<V>>(val locations: List<V>, val velocities: List<V>) {
    val energy
        get() = locations.indices.map {
            locations[it].absoluteValue * velocities[it].absoluteValue
        }.sum()

    fun graph(index: Int, selector: (V) -> Int): String {
        val loc = selector(locations[index])
        val nextLoc = loc + selector(velocities[index])
        return "|".padStart(21, '.').padEnd(41, '.')
            .run { substring(0, 20 - nextLoc) + "-" + substring(21 - nextLoc) }
            .run { substring(0, 20 + loc) + "*" + substring(21 + loc) }
    }

    fun printState(step: Int) {
        logWithTime("After $step steps:")
        locations.indices.forEach { i ->
            println("pos=${locations[i]} (${locations[i].absoluteValue}) vel=${velocities[i]} (${velocities[i].absoluteValue})")
        }
        println("energy is ${energy}")
    }

    fun step(): State<V> {
        // gravity
        val newV = velocities.mapIndexed { index, velocity ->
            val changes = locations
                .map { it - locations[index] } // distances
                .map { it.sign } // directions
            val v = changes.fold(velocity, { acc, change -> acc + change })
            v
        }

        // move
        val newL = locations.indices.map { locations[it] + newV[it] }

        return State(newL, newV)
    }


}


fun main() {
    val input = readAllLines("input-2019-12.txt")
//    val input = "<x=-1, y=0, z=2>\n<x=2, y=-10, z=-7>\n<x=4, y=-8, z=8>\n<x=3, y=5, z=-1>".lines()

    phase1(input)

    // phase 2 stupid and not really working yet...
    val x = calcOneDimension(input) { Vector1(it.x) }
        .also { logWithTime("x: $it")}
    val y = calcOneDimension(input) { Vector1(it.y) }
        .also { logWithTime("y: $it")}
    val z = calcOneDimension(input) { Vector1(it.z) }
        .also { logWithTime("z: $it")}

    println(x*y*z) //your answer is too low

}

fun calcOneDimension(
    input: List<String>,
    selector: (Vector3) -> Vector1
): Int {
    var state = initialState(input).let { v3 ->
        State(v3.locations.map(selector), v3.velocities.map(selector))
    }
    val cache = mutableMapOf<State<Vector1>, Int>()
    var iteration = 0

    while (!cache.containsKey(state)) {
        cache[state] = iteration
        iteration++
        state = state.step()
    }
    println("iteration $iteration was prev @ ${cache[state]}")
    return iteration
}

fun phase1(input: List<String>) {
    // phase 1
    var state = initialState(input)
    state.printState(0)
    repeat(1000) { state = state.step() }
    state.printState(1000)
}

fun initialState(input: List<String>): State<Vector3> {
    val locations = input
        .map { regex.matchEntire(it) ?: error("not matching '$it'") }
        .map { it.destructured }
        .map {
            Vector3(
                it.component2().toInt() * if (it.component1() == "-") -1 else 1,
                it.component4().toInt() * if (it.component3() == "-") -1 else 1,
                it.component6().toInt() * if (it.component5() == "-") -1 else 1
            )
        }

    val velocities = (locations.indices).map { Vector3(0, 0, 0) }

    return State(locations, velocities)
}

