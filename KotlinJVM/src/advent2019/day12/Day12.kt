package advent2019.day12

import advent2019.gcd
import advent2019.lcm
import advent2019.logWithTime
import advent2019.readAllLines
import kotlin.math.absoluteValue
import kotlin.math.sign

interface Vector<V> {
    val absoluteValue: Int
    val sign: V
    operator fun minus(o: V): V
    operator fun plus(o: V): V
}

data class Vector3(val x: Int, val y: Int, val z: Int) :
    Vector<Vector3> {
    override val absoluteValue get() = x.absoluteValue + y.absoluteValue + z.absoluteValue
    override val sign get() = Vector3(x.sign, y.sign, z.sign)
    override operator fun minus(o: Vector3): Vector3 =
        Vector3(x - o.x, y - o.y, z - o.z)
    override operator fun plus(o: Vector3): Vector3 =
        Vector3(x + o.x, y + o.y, z + o.z)
    override fun toString(): String =
        "<x=${x.toString().padStart(3)}, y=${y.toString().padStart(3)}, z=${y.toString().padStart(3)}>"
}

data class Vector1(val x: Int) :
    Vector<Vector1> {
    override val absoluteValue get() = x.absoluteValue
    override val sign get() = Vector1(x.sign)
    override operator fun minus(o: Vector1): Vector1 =
        Vector1(x - o.x)
    override operator fun plus(o: Vector1): Vector1 =
        Vector1(x + o.x)
}

data class State<V : Vector<V>>(val locations: List<V>, val velocities: List<V>) {
    val energy
        get() = locations.indices.map {
            locations[it].absoluteValue.toLong() * velocities[it].absoluteValue.toLong()
        }.sum()

    fun printState(step: Int) {
        logWithTime("After $step steps energy is $energy")
        locations.indices.forEach { i ->
            println("pos:${locations[i]}  vel:${velocities[i]}")
        }
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

    phase1(input, 1000)
        .also { logWithTime("phase1 result is $it") }

    phase2(input)
        .also { logWithTime("phase2 result is $it") }

}

fun phase1(input: List<String>, times: Int): Long {
    var state = initialState(input)
    state.printState(0)
    repeat(times) { state = state.step() }
    state.printState(times)
    return state.energy
}

fun phase2(input: List<String>): Long {
    val x = calcOneDimension(input) { Vector1(it.x) }
        .also { logWithTime("x plane cycle: $it") }
    val y = calcOneDimension(input) { Vector1(it.y) }
        .also { logWithTime("y plane cycle: $it") }
    val z = calcOneDimension(input) { Vector1(it.z) }
        .also { logWithTime("z plane cycle: $it") }

    return lcm(x, lcm(y, z))
        .also { logWithTime("lcm: $it") }
}

fun calcOneDimension(
    input: List<String>,
    selector: (Vector3) -> Vector1
): Long {
    var state = initialState(input).let { v3 ->
        State(v3.locations.map(selector), v3.velocities.map(selector))
    }
    val state0 = state
    var iteration = 0L

    do {
        iteration++
        state = state.step()
    } while (state != state0)
    return iteration
}

// ikr
val regex = Regex("<x=\\s*(-?)(\\d+),\\s*y\\s*=(-?)(\\d+),\\s*z\\s*=(-?)(\\d+)\\s*>")

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

