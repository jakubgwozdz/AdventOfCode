package advent2019.day03

import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlin.math.absoluteValue

fun main() {
    val input = Files.readAllLines(Paths.get("input-2019-03.txt"))
        .also { println("${Instant.now()}: data loaded") }
    val first = input.first()
    val second = input.drop(1).first()
    calculate(first, second, ::metric2)
}

typealias Place = Pair<Int, Int>

data class Crossing(val place: Place, val wire1Distance: Int, val wire2Distance: Int)

fun metric1(crossing: Crossing): Int {
    return crossing.place.first.absoluteValue + crossing.place.second.absoluteValue
}

fun metric2(crossing: Crossing): Int {
    return crossing.wire1Distance + crossing.wire2Distance
}

private fun movesForPart(part: String): List<Char> {
    val direction = part[0]
    val len = part.substring(1).toInt()
    return (1..len).map { direction }
}

private fun positionsByPlaces(wire: String): Map<Place, Int> {
    return wire.split(',')
        .map { it.trim() }
        .flatMap { part -> movesForPart(part) }
        .also { println("${Instant.now()}: moves counted") }
        .let { placesAndPositions(it) }
        .also { println("${Instant.now()}: places counted: ${it.size}") }
        .distinctBy { it.first }
        .toMap()
        .also { println("${Instant.now()}: distinct places ${it.size}") }
}

private fun placesAndPositions(moves: List<Char>): List<Pair<Place, Int>> {
    var acc: Place = 0 to 0
    return moves.mapIndexed { i, c ->
        acc = when (c) {
            'U' -> acc.first to acc.second + 1
            'R' -> acc.first + 1 to acc.second
            'D' -> acc.first to acc.second - 1
            'L' -> acc.first - 1 to acc.second
            else -> error(c)
        }
        acc to i + 1
    }
}


fun findCrossings(wire1: String, wire2: String): Collection<Crossing> {

    val places1 = positionsByPlaces(wire1)
    val places2 = positionsByPlaces(wire2)

    val intersect = places1.keys.intersect(places2.keys)
        .also { println("${Instant.now()}: intersections found: $it") }

    return intersect.map {
        Crossing(it, places1[it]!!, places2[it]!!)
    }
}

fun calculate(wire1: String, wire2: String, metric: (Crossing) -> Int): Int {
    val intersections = findCrossings(wire1, wire2)
    val distances =
        intersections.map { metric(it) }.also { println("${Instant.now()}: distances $it") }
    val closest = distances.min()!!.also { println("${Instant.now()}: result $it") }
    return closest
}

