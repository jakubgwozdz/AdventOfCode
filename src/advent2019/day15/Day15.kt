package advent2019.day15

import advent2019.day15.MapContent.values
import advent2019.intcode.Computer
import advent2019.intcode.Memory
import advent2019.intcode.parse
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.math.BigInteger.ZERO

fun main() {

    val program = readAllLines("input-2019-15.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    val memory = parse(program)
    val map = mapShip(memory)
    val start: Location = 0 to 0
    val end = map.filterValues { it == MapContent.Target }.keys.single()
    logWithTime("$start - $end")

    val part1 = shortestTo(end, map, mutableMapOf(start to emptyList()), emptyList())
        .also { logWithTime("$it") }!!
        .size
    logWithTime("Part1: Distance: $part1")

    // part2
    val cache = mutableMapOf<Location, List<Location>>(end to emptyList())
    val part2 = map.filter { (_, c) -> c.canGoThrough }
        .keys
        .map { shortestTo(it, map, cache, emptyList()) ?: error("cant get to $it") }
        .map { it.size }
        .max()!!

    logWithTime("Part2: Time to fill: $part2")

}

fun shortestTo(
    end: Location,
    map: Map<Location, MapContent>,
    cache: MutableMap<Location, List<Location>>,
    visited: List<Location>
): List<Location>? {
    return Direction.values()
        .map { end + it }
        .filter { !visited.contains(it) }
        .filter { map[it]?.canGoThrough ?: error("from $end to $it???") }
        .mapNotNull { newEnd ->
            cache[newEnd]
                ?: shortestTo(newEnd, map, cache, visited + newEnd)
                    ?.also { cache[newEnd] = it }
        }
        .minBy { it.size }
        ?.let { it + end }
}

typealias Location = Pair<Int, Int>
typealias Delta = Pair<Int, Int>

enum class Direction(val code: BigInteger, val delta: Delta) {
    N(1.toBigInteger(), -1 to 0),
    S(2.toBigInteger(), 1 to 0),
    W(3.toBigInteger(), 0 to -1),
    E(4.toBigInteger(), 0 to 1)
}

enum class MapContent(val code: BigInteger, val canGoThrough: Boolean) {
    Wall(0.toBigInteger(), false),
    Space(1.toBigInteger(), true),
    Target(2.toBigInteger(), true);
}

operator fun Location.plus(d: Direction) = this + d.delta
operator fun Location.plus(delta: Delta) = first + delta.first to second + delta.second
operator fun Location.minus(what: Location) = Direction.values().single { this == what + it }


fun mapShip(memory: Memory): Map<Location, MapContent> = runBlocking {
    var location = 0 to 0
    val map = mutableMapOf(location to MapContent.Space)
    val cameFrom = mutableMapOf<Location, Location>()
    val inBuffer = Channel<BigInteger>()
    val outBuffer = Channel<BigInteger>()
    val job = launch { Computer("ROBOT", memory, inBuffer, outBuffer).run() }
    while (job.isActive) {
        var direction = nextUnknown(location, map)

        if (direction == null) { // no more moves, go back
            val from = cameFrom[location]
            if (from != null) {
                direction = from - location
            } else {
                inBuffer.send(ZERO)
                continue
            }
        }

        inBuffer.send(direction.code)

        val nextLoc = location + direction
        val result = outBuffer.receive().run { values().single { this == it.code } }
        map[nextLoc] = result

        if (result.canGoThrough) {
            if (nextLoc != 0 to 0) cameFrom.computeIfAbsent(nextLoc) { location }
            location = nextLoc
        }
//        output(map)
//        logWithTime("loc = $location ; dir = $direction ; mapSize = ${map.size}")

    }
    coroutineContext.cancelChildren()
    map.toMap()
        .also { output(it) }
}

fun nextUnknown(location: Location, map: Map<Location, MapContent>) =
    Direction.values().firstOrNull { map[location + it] == null }

fun output(map: Map<Location, MapContent>) {
    val minY = map.keys.map { (y, x) -> y }.min() ?: error("no data")
    val maxY = map.keys.map { (y, x) -> y }.max() ?: error("no data")
    val minX = map.keys.map { (y, x) -> x }.min() ?: error("no data")
    val maxX = map.keys.map { (y, x) -> x }.max() ?: error("no data")
    (minY..maxY).map { y ->
        (minX..maxX).map { x ->
            when {
                map[y to x] == MapContent.Wall -> '#'
                map[y to x] == MapContent.Target -> 'X'
                map.keys.last() == y to x -> 'D'
                y == 0 && x == 0 -> '0'
                map[y to x] == MapContent.Space -> '.'
                else -> ' '
            }
        }.joinToString("")
    }.forEach { logWithTime(it) }
}

