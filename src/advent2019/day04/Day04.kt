package advent2019.day04

import advent2019.logWithTime

fun main() {
    countPossible("254032-789860").also { logWithTime("${it.size}") }
}

fun countPossible(range: String): Collection<Int> {
    val (from, to) = Regex("(\\d+)-(\\d+)").matchEntire(range)?.destructured
        ?: error("can't parse $range")
    logWithTime("from: $from, to: $to")
    return (from.toInt()..to.toInt())
        .filter { number ->
            isValid(number)
        }
        .also { logWithTime("$it") }
}

private fun isValid(number: Int): Boolean {
    val s = String.format("%06d", number)

    return (1 until s.length).all { i -> s[i - 1] <= s[i] } &&
            (1 until s.length).any { i -> s[i - 1] == s[i] } &&
            // part2:
            ('0'..'9').map { d -> s.count { c -> c == d } }.any { it == 2 }
}
