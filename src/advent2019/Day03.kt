package advent2019

import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.math.absoluteValue

fun main() {
    val input = Files.readAllLines(Paths.get("input-2019-03.txt"))
    part1(input.first(), input.drop(1).first())
}

private fun wireToPlaces(wire: String): Set<Pair<Int, Int>> {
    return wire.split(',')
        .map { it.trim() }
        .flatMap { part ->
            val direction = part[0]
            val len = part.substring(1).toInt()
            (1..len).map { direction }
        }
        .also { println("${Instant.now()}: moves counted") }
        .let {
            var acc = 0 to 0
            it.mapIndexed {i,c->
                acc = when (c) {
                    'U' -> acc.first to acc.second + 1
                    'R' -> acc.first + 1 to acc.second
                    'D' -> acc.first to acc.second - 1
                    'L' -> acc.first - 1 to acc.second
                    else -> error(c)
                }
                acc //to i
            }
        }
        .also { println("${Instant.now()}: places counted: ${it.size}") }
//        .distinctBy { it.first }
        .toHashSet()
        .also { println("${Instant.now()}: hashsetted :P") }
}


fun findCrossings(wire1: String, wire2: String): Set<Pair<Int, Int>> {

    val places1 = wireToPlaces(wire1)
    val places2 = wireToPlaces(wire2)

    val intersect = places1.intersect(places2).also { println("${Instant.now()}: intersections found: $it") }

    return intersect
}

fun part1(wire1: String, wire2: String): Int {
    val intersections = findCrossings(wire1, wire2)
    val distances = intersections.map { it.first.absoluteValue + it.second.absoluteValue }.also { println("${Instant.now()}: distances $it") }
    val closest = distances.min()!!.also { println("${Instant.now()}: $it") }
    return closest
}

//fun part2(wire1: String, wire2: String): Int {
//    val intersections = findCrossings(wire1, wire2)
//    val distances = intersections.map { it.first.absoluteValue + it.second.absoluteValue }.also { println(it) }
//    val closest = distances.min()!!.also { println("${Instant.now()}: $it") }
//    return closest
//}

