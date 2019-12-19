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

    var scan = 0..0
    (0 until 50).map { y ->
        repeat(scan.first) { print(" ") }
        val newLine = scan.map { x ->
            BigInteger.ONE == call(input, x, y)
                .also { print(it) }
        }
            .also { println() }
        val result = if (newLine.none { it }) scan.last + 1..scan.last
        else scan.first + newLine.indexOfFirst { it }..scan.first + newLine.indexOfLast { it }
        scan = (if (result.isEmpty()) scan.first else result.first)..result.last + 1
        result
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
