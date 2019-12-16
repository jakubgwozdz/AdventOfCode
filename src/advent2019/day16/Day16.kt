package advent2019.day16

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val input = readAllLines("input-2019-16.txt").single()
        .also { logWithTime("input length: ${it.length}")}
    val output = fft(input, 100).take(8)
        .also { logWithTime("part 1: $it")}

}

fun fftPattern(i: Int, l: Int): List<Int> {
    val p = listOf(0, 1, 0, -1)
    var t = 0
    return generateSequence { p[t++ % 4] }
        .flatMap { f -> sequence { repeat(i) { yield(f) } } }
        .drop(1)
        .take(l)
        .toList()
}

fun fftPhase(input: String): String {
    return input.mapIndexed { i, c ->
        val fftPattern = fftPattern(i + 1, input.length)
        val sum = input.map { "$it".toInt() }
            .zip(fftPattern)
            .map { (c, p) -> c * p }
            .sum()
        when {
            sum > 0 -> sum % 10
            sum < 0 -> (-sum) % 10
            else -> 0
        }
    }.joinToString("")
}

fun fft(input: String, iterations: Int) :String {
    return (1..iterations).fold(input) { i,_-> fftPhase(i)}
}

