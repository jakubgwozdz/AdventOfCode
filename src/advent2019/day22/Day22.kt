package advent2019.day22

import advent2019.logWithTime
import advent2019.readAllLines
import java.math.BigInteger

internal val Int.bi get() = toBigInteger()
internal val Long.bi get() = toBigInteger()

data class LinearOp(val a: BigInteger = 1.bi, val b: BigInteger = 0.bi, val deckSize: BigInteger) {

    fun normalize() = LinearOp((a + deckSize) % deckSize, (b + deckSize) % deckSize, deckSize)

    fun compose(other: LinearOp) = LinearOp(a * other.a, b * other.a + other.b, deckSize)

    fun apply(other: BigInteger) = (a * other + b) % deckSize

    fun repeat(times: Long): LinearOp {
        var powered = this
        val biTimes = times.bi
        val bitLength = biTimes.bitLength()
        val powers = (1..bitLength)
            .map { (it to powered).also { powered = (powered.compose(powered)).normalize() } }
            .toMap()

        return (1..bitLength).filter { biTimes.testBit(it - 1) }
            .map { powers[it] ?: error("won't happen") }
            .asReversed()
            .fold(LinearOp(1.bi, 0.bi, deckSize)) { a, e -> a.compose(e) }
    }
}

interface ShuffleOp {
    fun toLinearOp(deckSize: Long): LinearOp
    fun toInverseOp(deckSize: Long): LinearOp
}

val dealIncRegex = Regex("deal with increment (-?\\d+)")
val newStackRegex = Regex("deal into new stack")
val cutRegex = Regex("cut (-?\\d+)")

fun parse(input: List<String>, deckSize: Long): List<ShuffleOp> {
    return input.map {
        dealIncRegex.matchEntire(it)?.destructured?.run { IncrementOp(component1().toLong()) }
            ?: newStackRegex.matchEntire(it)?.destructured?.run { NewStackOp() }
            ?: cutRegex.matchEntire(it)?.destructured?.run { CutOp(component1().toLong()) }
            ?: error("'$it' is not proper op")
    }
}

class NewStackOp() : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp((-1).bi, (-1).bi, deckSize.bi)

    override fun toInverseOp(deckSize: Long) = LinearOp((-1).bi, (-1).bi, deckSize.bi)

}

class CutOp(private val cutPos: Long) : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(1.bi, -cutPos.bi, deckSize.bi)

    override fun toInverseOp(deckSize: Long) = LinearOp(1.bi, cutPos.bi, deckSize.bi)

}

class IncrementOp(private val increment: Long) : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(increment.bi, 0.bi, deckSize.bi)

    override fun toInverseOp(deckSize: Long) = LinearOp(increment.bi.modInverse(deckSize.bi), 0.bi, deckSize.bi)

}

class Deck(val deckSize: Long, input: List<String>, val times: Long = 1) {

    private val shuffleOps: List<ShuffleOp> = parse(input, deckSize)

    fun find(card: Long): Long {

        val biDeckSize = deckSize.bi

        val ops = shuffleOps
            .fold(LinearOp(1.bi, 0.bi, biDeckSize)) { a, s -> (a.compose(s.toLinearOp(deckSize))).normalize() }
            .repeat(times)

        val v = ops.apply(card.bi)
        return v.longValueExact()

    }

    fun cardAt(pos: Long): Long {

        val biDeckSize = deckSize.bi

        val ops = shuffleOps
            .asReversed()
            .fold(LinearOp(1.bi, 0.bi, biDeckSize)) { a, s -> (a.compose(s.toInverseOp(deckSize))).normalize() }
            .repeat(times)

        val v = ops.apply(pos.bi)
        return v.longValueExact()
    }

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