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

    (0 until 50).flatMap { y ->
        (0 until 50).map { x ->
            call(input, x, y)
                .also { print(it) }
        }.also { println() }
    }
        .count { o -> o == BigInteger.ONE }
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
