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

class Eris(val map: List<String>) {
    fun next(): Eris {
        return map.indices.map { y ->
            buildString {
                map[y].indices.forEach { x ->
                    val adj = listOf(y - 1 to x, y + 1 to x, y to x - 1, y to x + 1)
                        .count { test(it.first, it.second) }
                    val alive = if (test(y, x)) adj == 1 else adj == 1 || adj == 2
                    append(if (alive) '#' else '.')
                }
            }
        }.let { Eris(it) }
    }

    fun test(y: Int, x: Int): Boolean = y in map.indices && x in map[y].indices && map[y][x] == '#'

    val biodiversity: Int by lazy {
        map.mapIndexed { y: Int, l: String ->
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
    }
}