package advent2019.day22

import advent2019.logWithTime
import advent2019.modinv
import advent2019.readAllLines

internal val Long.bi get() = toBigInteger()

data class LinearOp(val deckSize: Long, val a: Long = 1, val b: Long = 0) {

    infix fun Long.modmul(other: Long) = ((bi * other.bi + deckSize.bi) % deckSize.bi).longValueExact()

    fun then(other: LinearOp) = LinearOp(deckSize, a modmul other.a, (other.a modmul b) + other.b)
    fun after(other: LinearOp) = LinearOp(deckSize, a modmul other.a, (a modmul other.b) + b)

    fun apply(other: Long) = ((a modmul other) + b + deckSize) % deckSize

    fun repeat(times: Long): LinearOp {
        var powered = this
        val biTimes = times.bi
        val bitLength = biTimes.bitLength()
        val powers = (1..bitLength)
            .map { (it to powered).also { powered = (powered.then(powered)) } }
            .toMap()

        return (1..bitLength).filter { biTimes.testBit(it - 1) }
            .map { powers[it] ?: error("won't happen") }
            .asReversed()
            .fold(LinearOp(deckSize)) { a, e -> a.then(e) }
    }

    override fun toString(): String {
        return "(a=$a, b=$b)"
    }

}

interface ShuffleOp {
    fun toLinearOp(deckSize: Long): LinearOp
    fun toInverseOp(deckSize: Long): LinearOp
}

val dealIncRegex = Regex("deal with increment (-?\\d+)")
val newStackRegex = Regex("deal into new stack")
val cutRegex = Regex("cut (-?\\d+)")

fun parse(input: List<String>): List<ShuffleOp> {
    return input.map {
        dealIncRegex.matchEntire(it)?.destructured?.run { IncrementOp(component1().toLong()) }
            ?: newStackRegex.matchEntire(it)?.destructured?.run { NewStackOp() }
            ?: cutRegex.matchEntire(it)?.destructured?.run { CutOp(component1().toLong()) }
            ?: error("'$it' is not proper op")
    }
}

class NewStackOp() : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(deckSize, (-1), (-1))

    override fun toInverseOp(deckSize: Long) = LinearOp(deckSize, (-1), (-1))

}

class CutOp(private val cutPos: Long) : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(deckSize, 1, -cutPos)

    override fun toInverseOp(deckSize: Long) = LinearOp(deckSize, 1, cutPos)

}

class IncrementOp(private val increment: Long) : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(deckSize, increment, 0)

    override fun toInverseOp(deckSize: Long) = LinearOp(deckSize, increment modinv deckSize, 0)

}

class Deck(val deckSize: Long, input: List<String>, val times: Long = 1) {

    private val shuffleOps: List<ShuffleOp> = parse(input)

    fun find(card: Long) = shuffleOps
        .fold(LinearOp(deckSize)) { a, s -> (s.toLinearOp(deckSize).after(a)) }
        .repeat(times)
        .apply(card)

    fun cardAt(pos: Long) = shuffleOps
        .asReversed()
        .fold(LinearOp(deckSize)) { a, s -> (s.toInverseOp(deckSize).after(a)) }
        .repeat(times).apply(pos)

}

fun main() {

    val input = readAllLines("data/input-2019-22.txt")

    Deck(10007, input)
        .find(2019)
        .also { logWithTime("part 1: $it") }

    Deck(119315717514047, input, 101741582076661)
        .cardAt(2020)
        .also { logWithTime("part 2: $it") }

}
