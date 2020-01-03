package advent2019.day22

import advent2019.logWithTime
import advent2019.readAllLines
import java.math.BigInteger

internal val Int.bi get() = toBigInteger()
internal val Long.bi get() = toBigInteger()

data class LinearOp(val a: BigInteger = 1.bi, val b: BigInteger = 0.bi) {

    fun inRange(deckSize: BigInteger) = LinearOp((a + deckSize) % deckSize, (b + deckSize) % deckSize)
    fun composeWith(other: LinearOp) = LinearOp(a * other.a, b * other.a + other.b)

}

interface ShuffleOp {
    fun move(from: Long): Long
    fun toLinearOp(): LinearOp
    fun toInverseOp(): LinearOp
}

val dealIncRegex = Regex("deal with increment (-?\\d+)")
val newStackRegex = Regex("deal into new stack")
val cutRegex = Regex("cut (-?\\d+)")

fun parse(input: List<String>, deckSize: Long): List<ShuffleOp> {
    return input.map {
        dealIncRegex.matchEntire(it)?.destructured?.run { IncrementOp(deckSize, component1().toLong()) }
            ?: newStackRegex.matchEntire(it)?.destructured?.run { NewStackOp(deckSize) }
            ?: cutRegex.matchEntire(it)?.destructured?.run { CutOp(deckSize, component1().toLong()) }
            ?: error("'$it' is not proper op")
    }
}

class NewStackOp(val deckSize: Long) : ShuffleOp {

    override fun move(from: Long): Long {
        return (deckSize - from - 1)
//                .also { println("$this: $from -> $it")  }
    }

    override fun toLinearOp(): LinearOp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toInverseOp(): LinearOp {
        return LinearOp(-1.bi, -1.bi)
    }

    override fun toString() = "NewStackOp()"
}

class CutOp(val deckSize: Long, val cutPos: Long) : ShuffleOp {
    private val c = if (cutPos < 0) cutPos + deckSize else cutPos
    override fun move(from: Long): Long {
        return ((deckSize * 2 - c + from) % deckSize)
    }

    override fun toLinearOp(): LinearOp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toInverseOp(): LinearOp {
        return LinearOp(1.bi, cutPos.bi)
    }

    override fun toString() = "CutOp($cutPos)"
}

class IncrementOp(val deckSize: Long, val increment: Long) : ShuffleOp {

    override fun move(from: Long): Long {
        return ((from * increment) % deckSize)
    }

    override fun toLinearOp(): LinearOp {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toInverseOp(): LinearOp {
        return LinearOp(increment.toBigInteger().modInverse(deckSize.bi), 0.bi)
    }

    override fun toString() = "IncrementOp(increment=$increment)"
}

class Deck(val deckSize: Long, input: List<String>, val times: Long = 1) {

    val shuffleOps: List<ShuffleOp> = parse(input, deckSize)

    fun find(card: Long): Long {
        return shuffleOps.fold(card) { acc, op -> op.move(acc) }
    }

    fun cardAt(pos: Long): Long {
        val ops = shuffleOps.asReversed()
            .fold(LinearOp()) { a, s -> a.composeWith(s.toInverseOp()).inRange(deckSize.bi) }

        println(ops)
        val v = (ops.a * pos.bi + ops.b) % deckSize.bi
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