package advent2019.day08

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val width = 25
    val height = 6
    val file = "data/input-2019-08.txt"

    val layers = readAllLines(file).single()
        .chunked(width * height) { it.toList() }

    layers
        .minBy { l -> l.count { it == '0' } }!!
        .let { l -> l.count { it == '1' } * l.count { it == '2' } }
        .let { logWithTime("part1 is $it") }

    layers
        .reduce { a, b -> a.indices.map { i -> if (a[i] != '2') a[i] else b[i] } }
        .chunked(width)
        .forEach { logWithTime(it) { replace('0', ' ') } }
}

