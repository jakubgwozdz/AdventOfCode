package advent2019.day22

import advent2019.logWithTime
import advent2019.readAllLines
import java.math.BigInteger

internal val Int.bi get() = toBigInteger()
internal val Long.bi get() = toBigInteger()

data class LinearOp(val a: BigInteger, val b: BigInteger, val deckSize: BigInteger) {

    constructor(a:Long, b:Long, deckSize:Long): this(a.bi, b.bi, deckSize.bi)
    constructor(deckSize:BigInteger): this(1.bi, 0.bi, deckSize)
    constructor(deckSize:Long): this(1.bi, 0.bi, deckSize.bi)

    fun normalize() = LinearOp((a + deckSize) % deckSize, (b + deckSize) % deckSize, deckSize)

    fun then(other: LinearOp) = LinearOp(a * other.a, b * other.a + other.b, deckSize)
    fun after(other: LinearOp) = LinearOp(a * other.a, b + a * other.b, deckSize)

    fun apply(other: Long) = ((a * other.bi + b) % deckSize).longValueExact()

    fun repeat(times: Long): LinearOp {
        var powered = this
        val biTimes = times.bi
        val bitLength = biTimes.bitLength()
        val powers = (1..bitLength)
            .map { (it to powered).also { powered = (powered.then(powered)).normalize() } }
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

    override fun toLinearOp(deckSize: Long) = LinearOp((-1), (-1), deckSize)

    override fun toInverseOp(deckSize: Long) = LinearOp((-1), (-1), deckSize)

}

class CutOp(private val cutPos: Long) : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(1, -cutPos, deckSize)

    override fun toInverseOp(deckSize: Long) = LinearOp(1, cutPos, deckSize)

}

class IncrementOp(private val increment: Long) : ShuffleOp {

    override fun toLinearOp(deckSize: Long) = LinearOp(increment, 0, deckSize)

    override fun toInverseOp(deckSize: Long) = LinearOp(increment.bi.modInverse(deckSize.bi).longValueExact(), 0, deckSize)

}

class Deck(val deckSize: Long, input: List<String>, val times: Long = 1) {

    private val shuffleOps: List<ShuffleOp> = parse(input)

    fun find(card: Long) = shuffleOps
        .fold(LinearOp(deckSize)) { a, s -> (a.then(s.toLinearOp(deckSize))).normalize() }
        .repeat(times)
        .apply(card)

    fun cardAt(pos: Long) = shuffleOps
        .asReversed()
        .fold(LinearOp(deckSize)) { a, s -> (a.then(s.toInverseOp(deckSize))).normalize() }
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
