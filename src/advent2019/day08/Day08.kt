package advent2019.day08

import advent2019.logWithTime
import advent2019.readFile

fun main() {
    val width = 25
    val height = 6
    val file = "input-2019-08.txt"

    val layers = readFile(file).first()
        .chunked(width * height)
        .map { it.toList().toList() }
    val minLayer = layers.minBy { l -> l.count { it == '0' } }!!
    val result = minLayer.count { it == '1' } * minLayer.count { it == '2' }
    logWithTime("part1 is $result")

    layers
        .reduce { a, b -> a.indices.map { i -> if (a[i] != '2') a[i] else b[i] } }
        .chunked(width)
        .forEach { logWithTime(it) { replace('0', ' ') } }
}

