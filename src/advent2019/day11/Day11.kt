package advent2019.day11

import advent2019.intcode.Intcode
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun main() {
    val program = readAllLines("data/input-2019-11.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    part1(program)

    paint(program) { hull[0 to 0] = 1 }
        .also {
            logWithTime("Part 2:")
            output(it)
            logWithTime("hull is ${it.size}")
        }

}

@ExperimentalCoroutinesApi
fun part1(program: String) = paint(program)
    .also {
        logWithTime("Part 1:")
        output(it)
        logWithTime("hull is ${it.size}")
    }
    .size


@ExperimentalCoroutinesApi
private fun paint(program: String, robotInitOp: PaintRobot.() -> Unit = {}): MutableMap<Pair<Int, Int>, Long> {
    val memory = parseIntcode(program)
    val robotToComp = Channel<Long>(Channel.CONFLATED)
    val compToRobot = Channel<Long>()
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

@ExperimentalCoroutinesApi
class PaintRobot(
    val inBuffer: ReceiveChannel<Long>,
    val outBuffer: SendChannel<Long>
) {
    var location = 0 to 0
    var direction = 0
    val hull = mutableMapOf<Pair<Int, Int>, Long>()

    suspend fun run() {
        while (!inBuffer.isClosedForReceive) {
            outBuffer.send(hull[location] ?: 0)
            val color = inBuffer.receive()//.also { logWithTime("color: $it") }
            val turn = inBuffer.receive()//.also { logWithTime("turn: $it") }
            hull[location] = color
            when (turn) {
                0L -> direction--
                1L -> direction++
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

fun output(hull: Map<Pair<Int, Int>, Long>) {
    val minY = hull.keys.map { (y, x) -> y }.min() ?: error("no data")
    val maxY = hull.keys.map { (y, x) -> y }.max() ?: error("no data")
    val minX = hull.keys.map { (y, x) -> x }.min() ?: error("no data")
    val maxX = hull.keys.map { (y, x) -> x }.max() ?: error("no data")
    (minY..maxY).map { y ->
        (minX..maxX).map { x ->
            if (hull[y to x] == 0L) '#' else ' '
        }.joinToString("")
    }.forEach { logWithTime(it) }
}
