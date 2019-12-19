package advent2019.day19

import advent2019.intcode.Computer
import advent2019.intcode.parse
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

fun main() {

    val input = readAllLines("input-2019-19.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    var lastY = -1
    var lastResult =  0 until 0

    var scan = 0..0
    (0 until 50).map { y ->
        repeat(scan.first) { print(" ") }
        val result = if (lastResult.count() < 2) {
            val newLine = scan.map { x ->
                BigInteger.ONE == call(input, x, y)
                    .also { print(it) }
            }
            if (newLine.none { it }) scan.last + 1..scan.last
            else scan.first + newLine.indexOfFirst { it }..scan.first + newLine.indexOfLast { it }
        } else {
            val f0 = call(input, lastResult.first, y)
                .also { print(it) }
            val newStart = if (f0 == BigInteger.ONE) lastResult.first else lastResult.first + 1
            val l1 = call(input, lastResult.last + 1, y)
                .also { repeat(lastResult.count() - 1) { print(" ") } }
                .also { print(it) }
            val newEnd = if (l1 != BigInteger.ONE) lastResult.last else lastResult.last + 1
            (newStart..newEnd)
        }

        scan = (if (result.isEmpty()) scan.first else result.first)..result.last + 1
        lastResult = result
        lastY = y
        result
            .also { println(" ; result = $result") }
    }.sumBy { it.count() }
        .also { logWithTime("part1: $it") }

}

fun call(program: String, x: Int, y: Int): BigInteger =
    runBlocking {

        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>()
        val computer = Computer("BEAM", parse(program), inChannel, outChannel)
        launch {
            computer.run()
            inChannel.close()
            outChannel.close()
        }
        inChannel.send(x.toBigInteger())
        inChannel.send(y.toBigInteger())
        outChannel.receive()
    }
