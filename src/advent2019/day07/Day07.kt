package advent2019.day07

import advent2019.intcode.Intcode
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.permutations
import advent2019.readAllLines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {

    val programStr = readAllLines("data/input-2019-07.txt").first()
        .also { logWithTime("Program length (chars): ${it.length}") }

    part1(programStr)
        .also { logWithTime("task1: $it") }

    part2(programStr)
        .also { logWithTime("task2: $it") }
}

fun part1(programStr: String) = permutations(5)
    .map { it.map { i -> i.toLong() } }
    .map { it to run(programStr, it) }
    .map { it.second }
    .max()

fun part2(programStr: String): Long? {
    return permutations(5)
        .map { it.map { i -> i.toLong() + 5 } }
        .map { it to run2(programStr, it) }
        .map { it.second }
        .max()
}


fun run(programStr: String, input: List<Long>): Long {
    var output = 0L
    input.forEachIndexed { index, ampl ->
        val program = parseIntcode(programStr)
        val inputBuffer = Channel<Long>()
        val outputBuffer = Channel<Long>()
        val computer = Intcode(program, inputBuffer, outputBuffer, 'A' + index)
        output = runBlocking {
            launch { computer.run() }
            inputBuffer.apply {
                send(ampl)
                send(output)
            }
            outputBuffer.receive()
        }
    }
    return output
}

fun run2(programStr: String, input: List<Long>): Long {

    val buffers = input.indices
        .map { Channel<Long>(Channel.UNLIMITED) }

    val computers = input.indices
        .map { 'A' + it }
        .mapIndexed { index, id ->
            Intcode(
                parseIntcode(programStr),
                buffers[index],
                buffers[(index + 1) % buffers.size],
                id
            )
        }

    runBlocking {
        computers.map {
            launch {
                it.run()
            }
        }
        launch {
            buffers.forEachIndexed { index, buffer ->
                buffer.send(input[index])
            }

            buffers[0].send(0L)
        }
    }

    return runBlocking {
        buffers[0].receive()
    }
}

