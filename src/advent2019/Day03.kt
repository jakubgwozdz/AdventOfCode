package advent2019

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.absoluteValue

fun main() {
    val input = Files.readAllLines(Paths.get("input-2019-03.txt"))
    findCrossing(input.first(), input.drop(1).first())
}

fun wireToMoves(wire: String): List<Char> = wire.split(',')
    .map { it.trim() }
    .flatMap { part ->
        val direction = part[0]
        val len = part.substring(1).toInt()
        (1..len).map { direction }
    }

fun movesToPlaces(moves: List<Char>): Set<Pair<Int, Int>> {
    return moves.fold(listOf(0 to 0)) { acc, c ->
        val last = acc.last()
        acc + when (c) {
            'U' -> last.first to last.second + 1
            'R' -> last.first + 1 to last.second
            'D' -> last.first to last.second - 1
            'L' -> last.first - 1 to last.second
            else -> error(c)
        }
    }.drop(1)
        .toSet()
}

fun findCrossing(wire1: String, wire2: String): Int {
    val moves1 = wireToMoves(wire1).also{println(it)}
    val moves2 = wireToMoves(wire2).also{println(it)}
    val places1 = movesToPlaces(moves1).also{println(it)}
    val places2 = movesToPlaces(moves2).also{println(it)}
    val intersect = places1.intersect(places2).also{println(it)}

    val distances = intersect.map { it.first.absoluteValue + it.second.absoluteValue }.also{println(it)}
    val closest = distances.sorted().first().also{println(it)}
    return closest
}
