package advent2019.day11

import advent2019.intcode.Intcode
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

fun main() {
    val program = readAllLines("data/input-2019-11.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    part1(program)

    paint(program) { hull[0 to 0] = ONE }
        .also {
            logWithTime("Part 2:")
            output(it)
            logWithTime("hull is ${it.size}")
        }

}

fun part1(program: String) = paint(program)
    .also {
        logWithTime("Part 1:")
        output(it)
        logWithTime("hull is ${it.size}")
    }
    .size


private fun paint(program: String, robotInitOp: PaintRobot.() -> Unit = {}): MutableMap<Pair<Int, Int>, BigInteger> {
    val memory = parseIntcode(program)
    val robotToComp = Channel<BigInteger>(Channel.CONFLATED)
    val compToRobot = Channel<BigInteger>()
    val robot = PaintRobot(compToRobot, robotToComp).apply(robotInitOp)

    runBlocking {
        val compJob = launch {
            Intcode(memory, robotToComp, compToRobot, "PAINT").run()
        }
        val robotJob = launch {
            robot.run()
        }
        compJob.join()
        coroutineContext.cancelChildren()

        compToRobot.close()
        robotJob.join()
        robotToComp.close()
    }

    return robot.hull
}

class PaintRobot(
    val inBuffer: ReceiveChannel<BigInteger>,
    val outBuffer: SendChannel<BigInteger>
) {
    var location = 0 to 0
    var direction = 0
    val hull = mutableMapOf<Pair<Int, Int>, BigInteger>()

    suspend fun run() {
        while (!inBuffer.isClosedForReceive) {
            outBuffer.send(hull[location] ?: ZERO)
            val color = inBuffer.receive()//.also { logWithTime("color: $it") }
            val turn = inBuffer.receive()//.also { logWithTime("turn: $it") }
            hull[location] = color
            when (turn) {
                ZERO -> direction--
                ONE -> direction++
                else -> error("unknown turn $turn")
            }
            if (direction < 0) direction += 4
            if (direction > 3) direction -= 4
            val (y, x) = location
            location = when (direction) {
                0 -> y - 1 to x
                1 -> y to x + 1
                2 -> y + 1 to x
                3 -> y to x - 1
                else -> error("$direction")
            }
//            output(hull)
//            logWithTime("hull is ${hull.size}")
        }
    }
}

fun output(hull: Map<Pair<Int, Int>, BigInteger>) {
    val minY = hull.keys.map { (y, x) -> y }.min() ?: error("no data")
    val maxY = hull.keys.map { (y, x) -> y }.max() ?: error("no data")
    val minX = hull.keys.map { (y, x) -> x }.min() ?: error("no data")
    val maxX = hull.keys.map { (y, x) -> x }.max() ?: error("no data")
    (minY..maxY).map { y ->
        (minX..maxX).map { x ->
            if (hull[y to x] == ONE) '#' else ' '
        }.joinToString("")
    }.forEach { logWithTime(it) }
}
