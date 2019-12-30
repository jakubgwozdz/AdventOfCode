package advent2019.day24

import advent2019.logWithTime
import advent2019.readAllLines

@ExperimentalStdlibApi
fun main() {
    val input = readAllLines("data/input-2019-24.txt")
        .also { logWithTime("lines: $it") }
    part1(input)
        .also { logWithTime("part 1: $it") }
    part2(input)
        .also { logWithTime("part 2: $it") }
}

@ExperimentalStdlibApi
fun part1(input: List<String>): Int {
    var eris = Eris(input)
    val s = mutableSetOf(eris.biodiversity)
    eris = eris.nextSinglePlane()
    while (eris.biodiversity !in s) {
        s += eris.biodiversity
        eris = eris.nextSinglePlane()
    }
    return eris.biodiversity
}

@ExperimentalStdlibApi
fun part2(input: List<String>): Int {
    var eris = Eris(input)
    repeat(200) { eris = eris.nextMultiPlane() }
    return eris.alives
}

@ExperimentalStdlibApi
data class Eris(val planes: Map<Int, Int>) {

    val alives: Int by lazy {
        planes.values
            .map { it.countOneBits() }
            .sum()
    }

    val biodiversity = planes[0] ?: 0

    constructor(map: List<String>) : this(mapOf(0 to biodiversity(map)))

    fun nextSinglePlane(): Eris = (0..4)
        .flatMap { y ->
            (0..4).map { x ->
                val adj = listOf(y - 1 to x, y + 1 to x, y to x - 1, y to x + 1)
                    .count { test(0, it.first, it.second) }
                val alive = if (test(0, y, x)) adj == 1 else adj == 1 || adj == 2
                (if (alive) 1 else 0) shl (y * 5 + x)
            }
        }
        .sum()
        .let { Eris(mapOf(0 to it)) }

    private fun test(p: Int, y: Int, x: Int): Boolean {
        return y in (0..4) && x in (0..4) && ((planes[p] ?: 0) shr (y * 5 + x)) % 2 == 1
    }

    data class T(val p: Int, val y: Int, val x: Int)

    private fun adjacent(p: Int, y: Int, x: Int): List<T> = when {
        y == 0 && x == 0 -> listOf(T(p, y + 1, x), T(p, y, x + 1), T(p - 1, 1, 2), T(p - 1, 2, 1))
        y == 0 && x == 4 -> listOf(T(p, y + 1, x), T(p, y, x - 1), T(p - 1, 1, 2), T(p - 1, 2, 3))
        y == 4 && x == 0 -> listOf(T(p, y - 1, x), T(p, y, x + 1), T(p - 1, 3, 2), T(p - 1, 2, 1))
        y == 4 && x == 4 -> listOf(T(p, y - 1, x), T(p, y, x - 1), T(p - 1, 3, 2), T(p - 1, 2, 3))
        y == 0 -> listOf(T(p, y + 1, x), T(p, y, x - 1), T(p, y, x + 1), T(p - 1, y + 1, 2))
        y == 4 -> listOf(T(p, y - 1, x), T(p, y, x - 1), T(p, y, x + 1), T(p - 1, y - 1, 2))
        x == 0 -> listOf(T(p, y, x + 1), T(p, y - 1, x), T(p, y + 1, x), T(p - 1, 2, x + 1))
        x == 4 -> listOf(T(p, y, x - 1), T(p, y - 1, x), T(p, y + 1, x), T(p - 1, 2, x - 1))
        y == 1 && x == 2 -> listOf(T(p, y - 1, x), T(p, y, x - 1), T(p, y, x + 1)) + (0..4).map { T(p + 1, 0, it) }
        y == 3 && x == 2 -> listOf(T(p, y + 1, x), T(p, y, x - 1), T(p, y, x + 1)) + (0..4).map { T(p + 1, 4, it) }
        y == 2 && x == 1 -> listOf(T(p, y, x - 1), T(p, y - 1, x), T(p, y + 1, x)) + (0..4).map { T(p + 1, it, 0) }
        y == 2 && x == 3 -> listOf(T(p, y, x + 1), T(p, y - 1, x), T(p, y + 1, x)) + (0..4).map { T(p + 1, it, 4) }

        else -> listOf(T(p, y - 1, x), T(p, y + 1, x), T(p, y, x - 1), T(p, y, x + 1))
    }

    fun nextMultiPlane(): Eris = planes.keys.let { ((it.min() ?: 0) - 1)..((it.max() ?: 0) + 1) }
        .map { p -> p to calculatePlane(p) }
        .filter { (_, s) -> s > 0 }
        .toMap()
        .let { Eris(it) }

    private fun calculatePlane(p: Int): Int = (0..4).flatMap { y -> (0..4).map { x -> y to x } }
        .filter { (y, x) -> y != 2 || x != 2 }
        .map { (y, x) -> calculatePoint(p, y, x) }
        .sum()

    private fun calculatePoint(p: Int, y: Int, x: Int): Int {
        val adj = adjacent(p, y, x)
            .onEach { (_, y1, x1) -> if (y1 == 2 && x1 == 2) error("invalid test for ($y,$x)") }
            .count { (p1, y1, x1) -> test(p1, y1, x1) }
        val alive = if (test(p, y, x)) adj == 1 else adj == 1 || adj == 2
        return (if (alive) 1 else 0) shl (y * 5 + x)
    }

}

fun biodiversity(map: List<String>): Int = map.also { if (map.size != 5) error("map $map rows != 5") }
    .mapIndexed { y: Int, l: String ->
        if (l.length != 5) error("line '$l'.length != 5")
        l.mapIndexed { x, c ->
            y * l.length + x to when (c) {
                '.' -> 0
                '#' -> 1
                else -> error("unknown '$c' @ row $y, column $x")
            }
        }
            .filter { (_, c) -> c == 1 }
            .map { (i, _) -> 1.shl(i) }
    }
    .flatten()
    .sum()
