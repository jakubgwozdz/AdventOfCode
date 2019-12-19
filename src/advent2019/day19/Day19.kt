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

    var pivot = -1 to (0 until 0)

    var nextScan = 0..0
    (0 until 50).map { y ->
        val lastResult = pivot.second
        repeat(nextScan.first) { print(" ") }
        val result = if (lastResult.count() < 2) {
            val newLine = nextScan.map { x ->
                call(input, x, y)
                    .also { print(if (it) "#" else ".") }
            }
            if (newLine.none { it }) nextScan.last + 1..nextScan.last
            else nextScan.first + newLine.indexOfFirst { it }..nextScan.first + newLine.indexOfLast { it }
        } else {
            var newStart = lastResult.first
            while (!call(input, newStart, y).also { print(if (it) "#" else ".") }) newStart++
            var newEnd = lastResult.last.coerceAtLeast(newStart)
            repeat(newEnd - newStart) { print(" ") }
            while (call(input, newEnd + 1, y).also { print(if (it) "#" else ".") }) newEnd++
            (newStart..newEnd)
        }

        nextScan = (if (result.isEmpty()) nextScan.first else result.first)..result.last + 1
        pivot = (y to result).also { println(" ; pivot = $it") }
        result
    }.sumBy { it.count() }
        .also { logWithTime("part1: $it") }

    val w = 100
    val h = 100

    var y = (pivot.first * w + pivot.second.first * h) / pivot.second.count()
    var matching = 0 to 0

    do {
        var t = findFor(y, pivot, input)
        pivot = y to t
        var b = findFor(y + h - 1, pivot, input)
        pivot = y + h - 1 to b
        val ww = t.last + 1 - b.first
        println("$y: $t - $b ($ww)")
        matching = b.first to y++
    } while (ww < w)

    logWithTime("part2: ${matching.first * 10000 + matching.second}")

}

fun findFor(y: Int, pivot: Pair<Int, IntRange>, input: String): IntRange {
    val (py, pr) = pivot
    var first = pr.first * y / py
    var last = pr.last * y / py
    while (!call(input, first, y)) first++
    while (call(input, first - 1, y)) first--

    while (!call(input, last, y)) last--
    while (call(input, last + 1, y)) last++
    return first..last
}


fun call(program: String, x: Int, y: Int): Boolean =
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
//            .also { print("$x,$y=$it;") }
    } == BigInteger.ONE
