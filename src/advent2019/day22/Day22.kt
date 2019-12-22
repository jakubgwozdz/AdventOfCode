package advent2019.day22

import advent2019.logWithTime
import advent2019.readAllLines

val dealIncRegex = Regex("deal with increment (-?\\d+)")
val newStackRegex = Regex("deal into new stack")
val cutRegex = Regex("cut (-?\\d+)")


fun deal(deckSize: Int, input: List<String>, times: Int = 1): List<Int> {

    val deck = IntArray(deckSize) { it }

    repeat(times) {
        input.forEach {
            dealIncRegex.matchEntire(it)?.destructured?.run { dealInc(deck, component1().toInt()) }
                ?: newStackRegex.matchEntire(it)?.destructured?.run { reverse(deck) }
                ?: cutRegex.matchEntire(it)?.destructured?.run { cut(deck, component1().toInt()) }
                ?: error("'$it' is not proper")
        }
    }
    return deck.toList()

}

fun reverse(deck: IntArray) {
    val table = IntArray(deck.size) { deck[deck.size - it - 1] }
    table.copyInto(deck)
}

fun cut(deck: IntArray, i: Int) {
    val j = if (i < 0) deck.size + i else i
    val table = IntArray(deck.size) { deck[(it + j) % deck.size] }
    table.copyInto(deck)
}

fun dealInc(deck: IntArray, increment: Int) {
    val table = IntArray(deck.size) { deck[it] }
    deck.indices.forEach { deck[it * increment % deck.size] = table[it] }
}

fun main() {

    val input = readAllLines("input-2019-22.txt")

    deal(10007, input).indexOfFirst { it == 2019 }
        .also { logWithTime("part 1: $it") }
}