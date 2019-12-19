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

    val scanner = Scanner(input)

    var pivot = -1 to (0 until 0)

    (0 until 50).map { y ->
        val lastResult = pivot.second
        repeat(lastResult.first) { print(" ") }
        if (lastResult.count() < 2) {
            var newStart = lastResult.first
            val limit = 20
            while (!scanner[newStart to y] && newStart < limit) newStart++
            if (newStart == limit) (lastResult.first until lastResult.first).also { println(" ; empty") }
            else {
                var newEndExclusive = newStart + 1
                while (scanner[newEndExclusive to y]) newEndExclusive++
                pivot = (y to (newStart until newEndExclusive)).also { println(" ; pivot = $it") }
                pivot.second
            }
        } else {
            var newStart = lastResult.first
            while (!scanner[newStart to y]) newStart++
            var newEnd = lastResult.last.coerceAtLeast(newStart)
            repeat(newEnd - newStart) { print(" ") }
            while (scanner[newEnd + 1 to y]) newEnd++
            pivot = (y to (newStart..newEnd)).also { println(" ; pivot = $it") }
            pivot.second
        }
    }.sumBy { it.count() }
        .also { logWithTime("part1: $it") }

    val w = 100
    val h = 100

    var y = (pivot.first * w + pivot.second.first * h) / pivot.second.count()
    var matching: Pair<Int, Int>

    do {
        val t = findFor(y, pivot, scanner)
        pivot = y to t
        val b = findFor(y + h - 1, pivot, scanner)
        pivot = y + h - 1 to b
        val ww = t.last + 1 - b.first
        println("$y: $t - $b ($ww)")
        matching = b.first to y++
    } while (ww < w)

    logWithTime("part2: ${matching.first * 10000 + matching.second}")

}

fun findFor(y: Int, pivot: Pair<Int, IntRange>, scanner: Scanner): IntRange {
    val (py, pr) = pivot
    var first = pr.first * y / py
    var last = pr.last * y / py
    while (!scanner[first to y]) first++
    while (scanner[first - 1 to y]) first--

    while (!scanner[last to y]) last--
    while (scanner[last + 1 to y]) last++
    return first..last
}


class Scanner(program: String) {

    val rom = parse(program)

    operator fun get(p: Pair<Int, Int>) = (call(p.first, p.second) == BigInteger.ONE)
        .also { print(if (it) "#" else ".") }

    fun call(x: Int, y: Int) =
        runBlocking {

            val inChannel = Channel<BigInteger>()
            val outChannel = Channel<BigInteger>()
            val computer = Computer("BEAM", rom.copy(), inChannel, outChannel)
            launch {
                computer.run()
                inChannel.close()
                outChannel.close()
            }
            inChannel.send(x.toBigInteger())
            inChannel.send(y.toBigInteger())
            outChannel.receive()
        }
}
