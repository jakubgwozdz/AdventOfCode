package advent2019.day22

import advent2019.logWithTime
import advent2019.modinv
import advent2019.readAllLines

interface ShuffleOp {
    fun invoke(from: Long): Long
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
    override fun invoke(from: Long): Long {
        return (deckSize - from - 1)
//                .also { println("$this: $from -> $it")  }
    }

    override fun toString(): String {
        return "NewStackOp()"
    }
}

class CutOp(val deckSize: Long, val cutPos: Long) : ShuffleOp {
    private val c = if (cutPos < 0) cutPos + deckSize else cutPos
    override fun invoke(from: Long): Long {
        return ((deckSize * 2 - c + from) % deckSize)
    }

    override fun toString(): String {
        return "CutOp($cutPos)"
    }
}

private val invmodCache: MutableMap<Long, Long> = mutableMapOf()

class IncrementOp(val deckSize: Long, val increment: Long) : ShuffleOp {
    val invmod by lazy {
        invmodCache.computeIfAbsent(increment) { it -> modinv(it, deckSize) } // TODO https://planetcalc.com/3298/
    }

    override fun invoke(from: Long): Long {
//            return ((from * invmod) % deckSize)
        return ((from * increment) % deckSize)
//                .also { println("$this: $from -> $it")  }

    }

    override fun toString(): String {
        return "IncrementOp(increment=$increment)"
    }
}

class Deck(val deckSize: Long, val shuffleOps: List<ShuffleOp>, val times: Long = 1) {
    constructor(input: List<String>, deckSize: Long, times: Long = 1) : this(deckSize, parse(input, deckSize), times)

    fun find(card: Long): Long {
        return shuffleOps.fold(card) { acc, op -> op.invoke(acc) }
    }
}

fun main() {

    val input = readAllLines("data/input-2019-22.txt")

    Deck(input, 10007)
        .find(2019)
        .also { logWithTime("part 1: $it") }

////    tests...
//
//    val p2deck = Deck(10007)
//        .apply { shuffle(input) }
//
//    var i = 0L
//    var j = 2020L
//    do {
//        j = p2deck.find(j)
//        i++
//        if (i % 1000 == 0L) print(".")
//        if (i % 100000 == 0L) println(" $i")
//    } while (j != 2020L)
//    logWithTime("cycle: $i")
//
//    Deck(119315717514047)
//        .apply { shuffle(input, 101741582076661) }
//        .find(2020)
//        .also { logWithTime("part 2: $it") }
}