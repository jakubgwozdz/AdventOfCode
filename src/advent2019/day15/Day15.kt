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

    logWithTime("Part1: Blocks: ${part1(program)}")

//    logWithTime("Part2: Score: ${part2(program)}")

}

fun part1(program: String): Int {
    val memory = parse(program)

    val map = mapShip(memory)
    val start = 0 to 0
    val end = map.filterValues { it == MapContent.Target }.keys.single()

    logWithTime("$start - $end")

    return 0
}

typealias Location = Pair<Int, Int>
typealias Delta = Pair<Int, Int>

enum class Direction(val code: BigInteger, val delta: Delta) {
    N(1.toBigInteger(), -1 to 0),
    S(2.toBigInteger(), 1 to 0),
    W(3.toBigInteger(), 0 to -1),
    E(4.toBigInteger(), 0 to 1)
}

operator fun Location.plus(d: Direction) = this + d.delta
operator fun Location.plus(delta: Delta) = first + delta.first to second + delta.second
operator fun Location.minus(what: Location) = Direction.values().single { this == what + it }


enum class MapContent(val code: BigInteger) {
    Wall(0.toBigInteger()), Space(1.toBigInteger()), Target(2.toBigInteger());

    companion object {
        fun fromCode(c: BigInteger): MapContent {
            return values().single { it.code == c }
        }
    }
}


fun mapShip(memory: Memory): Map<Location, MapContent> = runBlocking {
    var location = 0 to 0
    val map = mutableMapOf(location to MapContent.Space)
    val cameFrom = mutableMapOf<Location, Location>()
    val inBuffer = Channel<BigInteger>()
    val outBuffer = Channel<BigInteger>()
    val job = launch { Computer("ROBOT", memory, inBuffer, outBuffer).run() }
    while (true) {
        var direction = nextUnknown(location, map)
        if (direction == null) { // no more moves, go back
            val from = cameFrom[location]
            if (from == null) {
                inBuffer.send(ZERO)
                break
            }
            direction = from - location
        }

        inBuffer.send(direction.code)

        val nextLoc = location + direction
        val result = MapContent.fromCode(outBuffer.receive())
        map[nextLoc] = result

        if (result != MapContent.Wall) {
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

fun nextUnknown(location: Location, map: Map<Location, MapContent>) = when {
    !map.containsKey(location + Direction.N) -> Direction.N
    !map.containsKey(location + Direction.S) -> Direction.S
    !map.containsKey(location + Direction.W) -> Direction.W
    !map.containsKey(location + Direction.E) -> Direction.E
    else -> null
}
//fun nextUnknown(location: Location, map: Map<Location, MapContent>) =
//    Direction.values().singleOrNull { !map.containsKey(location + it) }

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

