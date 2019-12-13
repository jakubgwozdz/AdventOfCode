package advent2019.day09

import advent2019.intcode.Computer
import advent2019.intcode.parse
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.math.BigInteger.ONE

fun main() {

    val programStr = readAllLines("input-2019-09.txt").first()
        .also { logWithTime("Program length (chars): ${it.length}") }

    run1(programStr, listOf(ONE))
        .also { logWithTime("part1: $it") }

    run1(programStr, listOf(2.toBigInteger()))
        .also { logWithTime("part2: $it") }
}

fun run1(programStr: String, input: List<BigInteger>): List<BigInteger> {

    val inBuffer = Channel<BigInteger>()
    val outBuffer = Channel<BigInteger>(Channel.UNLIMITED)
    val computer = Computer(
        "BOOST",
        parse(programStr),
        inBuffer,
        outBuffer
    )

    return runBlocking {
        val job = launch {
            computer.run()
        }
        input.forEach { inBuffer.send(it) }
        job.join()
        inBuffer.close()
        outBuffer.close()
        outBuffer.toList()
    }
}

