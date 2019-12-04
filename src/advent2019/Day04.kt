package advent2019

import java.time.LocalTime.now

fun log(msg: String) {
    println("${now()}: $msg")
}

fun main() {
    countPossible("254032-789860").also { log("${it.size}") }
}

fun countPossible(range: String): Collection<Int> {
    val (from, to) = Regex("(\\d+)-(\\d+)").matchEntire(range)?.destructured ?: error("can't parse $range")
    log ( "from: $from, to: $to" )
    return (from.toInt()..to.toInt())
        .filter { number ->
            isValid(number)
        }
        .also { log("$it") }
}

private fun isValid(number: Int): Boolean {
    val s = String.format("%06d", number)

    return (1 until s.length).all { i -> s[i - 1] <= s[i] } &&
            (1 until s.length).any { i -> s[i - 1] == s[i] } &&
            // part2:
            ('0'..'9').map { d -> s.count { c -> c == d } }.any { it == 2 }
}
