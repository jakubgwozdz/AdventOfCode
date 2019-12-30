package advent2019.day24

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val input = readAllLines("data/input-2019-24.txt")
        .also { logWithTime("lines: $it") }
    part1(input)
        .also { logWithTime("part 1: $it") }
}

fun part1(input: List<String>): Int {
    var eris = Eris(input)
    val s = mutableSetOf(eris.biodiversity)
    eris = eris.next()
    while (eris.biodiversity !in s) {
        s += eris.biodiversity
        eris = eris.next()
    }
    return eris.biodiversity
}

fun part2(input: List<String>): Int {
    TODO()
}

data class Eris(val biodiversity: Int, val r: Int, val c: Int) {

    constructor(map: List<String>) : this(biodiversity(map), map.size, map[0].length)

    fun next(): Eris = (0 until r)
        .flatMap { y ->
            (0 until c).map { x ->
                val adj = listOf(y - 1 to x, y + 1 to x, y to x - 1, y to x + 1)
                    .count { test(it.first, it.second) }
                val alive = if (test(y, x)) adj == 1 else adj == 1 || adj == 2
                (if (alive) 1 else 0) shl (y * c + x)
            }
        }
        .sum()
        .let { Eris(it, r, c) }

    fun test(y: Int, x: Int): Boolean {
        return y in (0 until r) && x in (0 until c) && (biodiversity shr (y * c + x)) % 2 == 1
    }

}

fun biodiversity(map: List<String>): Int = map.mapIndexed { y: Int, l: String ->
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
