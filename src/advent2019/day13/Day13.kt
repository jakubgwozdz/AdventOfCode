package advent2019.day13

import advent2019.intcode.ChannelOutBuffer
import advent2019.intcode.Intcode
import advent2019.intcode.InBuffer
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

fun main() {

    val program = readAllLines("data/input-2019-13.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    logWithTime("Part1: Blocks: ${part1(program)}")

    logWithTime("Part2: Score: ${part2(program)}")


}

fun part1(program: String): Int {
    val memory = parseIntcode(program)

    return runBlocking {
        val outBuffer = Channel<BigInteger>(Channel.UNLIMITED)
        val comp = launch {
            Intcode(memory, Channel(), outBuffer, "ARCADE", false).run()
        }
        comp.join()
        outBuffer.close()
        outBuffer.toList()
    }
        .chunked(3)
        .map { (it[0].toInt() to it[1].toInt()) to it[2].toInt() }
        .toMap()
        .also { display(it, 0) }
        .let { noOfBlocks(it) }
}

fun part2(program: String): Int {
    val memory = parseIntcode(program)
    memory[BigInteger.ZERO] = 2.toBigInteger()

    return runBlocking {
        val tiles = mutableMapOf<Pair<Int, Int>, Int>()
        val output = Channel<BigInteger>(Channel.UNLIMITED)
        val input = object : InBuffer<BigInteger> {
            override suspend fun receive(): BigInteger {
                return updateGame(tiles, output).toBigInteger()
            }
        }
        val comp = launch {
            Intcode(memory, input, ChannelOutBuffer("ARCADE", output), "ARCADE").run()
        }

        comp.join()
        updateGame(tiles, output)
        coroutineContext.cancelChildren()
        score(tiles)
    }
}

suspend fun updateGame(tiles: MutableMap<Pair<Int, Int>, Int>, output: Channel<BigInteger>): Int {
    while (!output.isEmpty) {
        val x = output.receive().toInt()
        val y = output.receive().toInt()
        val v = output.receive().toInt()
        tiles[x to y] = v
        //                    if (v == 3) println("pad moved to ${x to y}")
        //                    if (v == 4) println("ball moved to ${x to y}")
    }
    val joystick = joystick(tiles)
    display(tiles, joystick)
    return joystick
}


private fun joystick(tiles: Map<Pair<Int, Int>, Int>): Int {
    val paddle = paddle(tiles)
    val ball = ball(tiles)
    return when {
        paddle.first == ball.first -> 0
        paddle.first < ball.first -> 1
        paddle.first > ball.first -> -1
        else -> error("paddle: $paddle, ball: $ball")
    }
}

private fun noOfBlocks(tiles: Map<Pair<Int, Int>, Int>) =
    tiles.filter { it.value == 2 }.size

private fun score(tiles: Map<Pair<Int, Int>, Int>) =
    tiles[-1 to 0] ?: 0

private fun paddle(tiles: Map<Pair<Int, Int>, Int>) =
    tiles.filter { it.value == 3 }.keys.singleOrNull() ?: -1 to -1

private fun ball(tiles: Map<Pair<Int, Int>, Int>) =
    tiles.filter { it.value == 4 }.keys.singleOrNull() ?: -1 to -1

fun screen(tiles: Map<Pair<Int, Int>, Int>): List<String> {
//    val minX = tiles.map { it.key.first }.min()!!
    val maxX = tiles.map { it.key.first }.max()!!
//    val minY = tiles.map { it.key.second }.min()!!
    val maxY = tiles.map { it.key.second }.max()!!

    return (0..maxY).map { y ->
        (0..maxX).map { x ->
            val tile = tiles[x to y]
            when (tile) {
                null -> ' '
                0 -> ' '
                1 -> '#'
                2 -> '%'
                3 -> '-'
                4 -> '*'
                else -> error("$tile @ $x,$y")
            }
        }.joinToString("")
    }
}

fun display(
    tiles: Map<Pair<Int, Int>, Int>,
    joysick: Int
) {
    (screen(tiles) + (
            "score: ${score(tiles)}".padEnd(20)
                    + "ball: ${ball(tiles)}".padEnd(20)
                    + "paddle: ${paddle(tiles)}".padEnd(20)
                    + "joystick: ${joysick}"
            )).joinToString("\n")
        .also { println(it) }
}

