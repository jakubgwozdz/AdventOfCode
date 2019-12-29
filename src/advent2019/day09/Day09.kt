package advent2019.day09


import advent2019.intcode.Intcode
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {

    val programStr = readAllLines("data/input-2019-09.txt").first()
        .also { logWithTime("Program length (chars): ${it.length}") }

    part1(programStr)
        .also { logWithTime("part1: $it") }

    part2(programStr)
        .also { logWithTime("part2: $it") }
}

fun part1(programStr: String) = run1(programStr, listOf(1L)).single()

fun part2(programStr: String) = run1(programStr, listOf(2L)).single()

fun run1(programStr: String, input: List<Long>): List<Long> {

    val inBuffer = Channel<Long>()
    val outBuffer = Channel<Long>(Channel.UNLIMITED)
    val computer = Intcode(parseIntcode(programStr), inBuffer, outBuffer, "BOOST")

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

