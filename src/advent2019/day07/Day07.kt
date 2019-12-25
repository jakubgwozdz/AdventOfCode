package advent2019.day07

import advent2019.intcode.Intcode
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.permutations
import advent2019.readAllLines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

fun main() {

    val programStr = readAllLines("data/input-2019-07.txt").first()
        .also { logWithTime("Program length (chars): ${it.length}") }

    permutations(5)
        .map { it.map { i -> i.toBigInteger() } }
        .map { it to run(programStr, it) }
        .maxBy { it.second }
        .also { logWithTime("task1: $it") }

    permutations(5)
        .map { it.map { i -> i.toBigInteger() + 5.toBigInteger() } }
        .map { it to run2(programStr, it) }
        .maxBy { it.second }
        .also { logWithTime("task2: $it") }
}

fun run(programStr: String, input: List<BigInteger>): BigInteger {
    var output = BigInteger.ZERO
    input.forEachIndexed { index, ampl ->
        val program = parseIntcode(programStr)
        val inputBuffer = Channel<BigInteger>()
        val outputBuffer = Channel<BigInteger>()
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

fun run2(programStr: String, input: List<BigInteger>): BigInteger {

    val buffers = input.indices
        .map { Channel<BigInteger>(Channel.UNLIMITED) }

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

            buffers[0].send(BigInteger.ZERO)
        }
    }

    return runBlocking {
        buffers[0].receive()
    }
}

