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

    val map = drawMap(parse(input))

    part1(map)
        .also { logWithTime("part 1: $it") }

    val program2 = parse(input).also { it[0.toBigInteger()] = 2.toBigInteger() }

    val mainRoutine = "A,B,A,B,A,C,B,C,A,C"
    val functionA = "L,6,R,12,L,6"
    val functionB = "R,12,L,10,L,4,L,6"
    val functionC = "L,10,L,10,L,4,L,6"

    runBlocking {
        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>(Channel.UNLIMITED)
        val job = launch {
            Computer("ASCII", program2, inChannel, outChannel).run()
            inChannel.close()
            outChannel.close()
        }
        val linesChannel = Channel<String>()
        val resultChannel = Channel<BigInteger>()

        val outJob = launch {
            var lastData = ZERO
            val chF = outChannel.consumeAsFlow()
                .onEach { lastData = it }
                .map { it.toInt().toChar() }

            val linesFlow = flow {
                val buffer = mutableListOf<Char>()
                chF.collect { v ->
                    buffer.add(v)
                    if (v == '\n') {
                        buildString(buffer.size) { buffer.forEach { append(it) } }
                            .also { emit(it) }
//                                emit(buffer.toCharArray().concatToString())
                        buffer.clear()
                    }
                }
            }
                .onEach { print(it) }
                .collect()
            resultChannel.send(lastData)

            resultChannel.close()
        }

//        linesChannel.consumeAsFlow()
//            .onEach { }
        inChannel.writeln(mainRoutine)
        delay(10)
        inChannel.writeln(functionA)
        delay(10)
        inChannel.writeln(functionB)
        delay(10)
        inChannel.writeln(functionC)
        delay(10)
        inChannel.writeln("n")

        job.join()

        resultChannel.receive()
//        o.last()
    }
        .also { logWithTime("part 2: $it") }
}

suspend fun SendChannel<BigInteger>.writeln(msg: String) {
    println(msg)
    msg.map { it.toInt().toBigInteger() }.forEach { send(it) }
    send('\n'.toInt().toBigInteger())
}

private fun part1(map: List<String>): Int {
    val intersections = (1..map.size - 2)
        .flatMap { y ->
            (1..map[y].length - 2).map { x -> y to x }
        }
        .filter { (y, x) -> map[y][x] == '#' && map[y - 1][x] == '#' && map[y][x - 1] == '#' && map[y + 1][x] == '#' && map[y][x + 1] == '#' }

    val alignment = intersections.map { (y, x) -> y * x }.sum()
    return alignment
}

@FlowPreview
@ExperimentalStdlibApi
private fun drawMap(program: Memory): List<String> {

    val map = runBlocking {
        val outChannel = Channel<BigInteger>(Channel.UNLIMITED)
        val job = launch {
            Computer("ASCII", program, Channel(), outChannel).run()
        }
        val flow = outChannel.consumeAsFlow()
            .map { it.toInt().toChar() }

        job.join()
        outChannel.close()
        flow.toList().toCharArray().concatToString().lines()
    }
        .also { map -> map.forEach { logWithTime(it) } }
    return map
}
