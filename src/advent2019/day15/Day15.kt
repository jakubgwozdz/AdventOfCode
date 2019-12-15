package advent2019.day15

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

fun main() {

    val program = readAllLines("input-2019-15.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    logWithTime("Part1: Blocks: ${part1(program)}")

//    logWithTime("Part2: Score: ${part2(program)}")

}

fun part1(program: String): Int {
    val memory = parse(program)

    val map = mapShip(memory)
    output(map)
    val start = 0 to 0
    val end = map.filterValues { it == 2 }.keys.single()

    logWithTime("$start - $end")

    return 0
}

fun mapShip(memory: Memory): Map<Pair<Int, Int>, Int> = runBlocking {
    var location = 0 to 0
    var direction = 1
    val map = mutableMapOf(location to direction)
    val inBuffer = Channel<BigInteger>()
    val outBuffer = Channel<BigInteger>()
    var moves = 0
//        val inBuffer = ChannelInBuffer("ROBOT", Channel())
//        val outBuffer = ChannelOutBuffer("ROBOT", Channel())
    val robot = Computer("ROBOT", memory, inBuffer, outBuffer)
    launch { robot.run() }
    var hitWall = false
    var found = false
    while (!found) {
//            println()
        val nextLoc = nextLocation(direction, location)
        inBuffer.send(direction.toBigInteger())
        val result = outBuffer.receive().toInt()
        map[nextLoc] = result
        if (result == 0) {
            // WALL
            hitWall = true
            direction = turnRight(direction)
        } else {
            if (hitWall) {
                location = nextLoc
                moves++
                direction = turnLeft(direction)
            }
        }
        found = result == 2
    }
    coroutineContext.cancelChildren()
    map.toMap()
}

fun nextLocation(direction: Int, location: Pair<Int, Int>): Pair<Int, Int> = when (direction) {
    1 -> location.first - 1 to location.second
    2 -> location.first + 1 to location.second
    3 -> location.first to location.second - 1
    4 -> location.first to location.second + 1
    else -> error("$direction")
}

fun turnLeft(direction: Int): Int = when (direction) {
    1 -> 3
    2 -> 4
    3 -> 2
    4 -> 1
    else -> error("$direction")
}

fun turnRight(direction: Int): Int = when (direction) {
    1 -> 4
    2 -> 3
    3 -> 1
    4 -> 2
    else -> error("$direction")
}

fun output(map: Map<Pair<Int, Int>, Int>) {
    val minY = map.keys.map { (y, x) -> y }.min() ?: error("no data")
    val maxY = map.keys.map { (y, x) -> y }.max() ?: error("no data")
    val minX = map.keys.map { (y, x) -> x }.min() ?: error("no data")
    val maxX = map.keys.map { (y, x) -> x }.max() ?: error("no data")
    (minY..maxY).map { y ->
        (minX..maxX).map { x ->
            when {
                map[y to x] == 0 -> '#'
                map[y to x] == 2 -> 'X'
                map.keys.last() == y to x -> 'D'
                y == 0 && x==0 -> '0'
                map[y to x] == 1 -> '.'
                else -> ' '
            }
        }.joinToString("")
    }.forEach { logWithTime(it) }
}

