package advent2019.day17

import advent2019.intcode.Computer
import advent2019.intcode.Memory
import advent2019.intcode.parse
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import java.math.BigInteger
import java.math.BigInteger.ZERO

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalStdlibApi
fun main() {
    val input = readAllLines("input-2019-17.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    val program1 = parse(input)
    val map = drawMap(program1)

    alignment(map)
        .also { logWithTime("part 1: $it") }

    val program2 = parse(input).also { it[0.toBigInteger()] = 2.toBigInteger() }

    val mainRoutine = "A,B,A,B,A,C,B,C,A,C"
    val functionA = "L,6,R,12,L,6"
    val functionB = "R,12,L,10,L,4,L,6"
    val functionC = "L,10,L,10,L,4,L,6"

    moveRobot(program2, mainRoutine, functionA, functionB, functionC)
        .also { logWithTime("part 2: $it") }
}


@Suppress("BlockingMethodInNonBlockingContext")
private fun Flow<Char>.fullLines(): Flow<String> = flow {
    val builder = StringBuilder()
    collect {
        when (it) {
            '\n' -> emit(builder.toString()).also { builder.clear() }
            else -> builder.append(it)
        }
    }
}

suspend fun SendChannel<BigInteger>.writeln(msg: String) {
    println(msg)
    msg.map { it.toInt().toBigInteger() }.forEach { send(it) }
    send('\n'.toInt().toBigInteger())
}

private fun List<String>.innerIndices() = (1..size - 2)
    .flatMap { y -> (1..this[y].length - 2).map { x -> y to x } }

private fun List<String>.hasIntersection(row: Int, column: Int): Boolean =
    (this[row][column] == '#'
            && this[row - 1][column] == '#' && this[row][column - 1] == '#'
            && this[row + 1][column] == '#' && this[row][column + 1] == '#')

private fun alignment(map: List<String>): Int =
    map.innerIndices()
        .filter { (row, column) -> map.hasIntersection(row, column) }
        .map { (row, column) -> row * column }
        .sum()

@FlowPreview
@ExperimentalStdlibApi
private fun drawMap(program: Memory): List<String> = runBlocking {
    val inChannel = Channel<BigInteger>()
    val outChannel = Channel<BigInteger>()
    val computer = Computer("ASCII", program, inChannel, outChannel)
    launch {
        computer.run()
        inChannel.close()
        outChannel.close()
    }
    outChannel.consumeAsFlow()
        .map { it.toInt().toChar() }
        .fullLines()
        .toList()
}

@ExperimentalCoroutinesApi
@FlowPreview
private fun moveRobot(
    program: Memory,
    mainRoutine: String,
    functionA: String,
    functionB: String,
    functionC: String
): BigInteger {
    var lastData = ZERO!!
    return runBlocking {
        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>()
        val computer = Computer("ASCII", program, inChannel, outChannel)
        launch {
            computer.run()
            inChannel.close()
            outChannel.close()
        }
        outChannel.consumeAsFlow()
            .onEach { lastData = it }
            .map { it.toInt().toChar() }
            .fullLines()
            .onEach {
                println(it)
                if (it == "Main:") inChannel.writeln(mainRoutine)
                if (it == "Function A:") inChannel.writeln(functionA)
                if (it == "Function B:") inChannel.writeln(functionB)
                if (it == "Function C:") inChannel.writeln(functionC)
                if (it == "Continuous video feed?") inChannel.writeln("n")
            }
            .collect()
            .let { lastData }
    }
}
