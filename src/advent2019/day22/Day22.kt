package advent2019.day22

import advent2019.logWithTime
import advent2019.modinv
import advent2019.readAllLines

val dealIncRegex = Regex("deal with increment (-?\\d+)")
val newStackRegex = Regex("deal into new stack")
val cutRegex = Regex("cut (-?\\d+)")

interface ShuffleOp {
    fun invoke(from: Long): Long
}

class Deck(val deckSize: Long) {

    val shuffleOps: MutableList<ShuffleOp> = mutableListOf()

    fun shuffle(input: List<String>, times: Long = 1) {
        val ops = parse(input)
        repeat(times.toInt()) { shuffleOps += ops }
    }

     fun find(card: Long): Long {
        return shuffleOps.fold(card) { acc, op -> op.invoke(acc) }
//        return shuffleOps.foldRight(card) { op, acc -> op.invoke(acc) }
    }

    fun parse(input: List<String>): List<ShuffleOp> {
        return input.map {
            dealIncRegex.matchEntire(it)?.destructured?.run { IncrementOp(component1().toLong()) }
                ?: newStackRegex.matchEntire(it)?.destructured?.run { NewStackOp() }
                ?: cutRegex.matchEntire(it)?.destructured?.run { CutOp(component1().toLong()) }
                ?: error("'$it' is not proper op")
        }
    }

    inner class NewStackOp : ShuffleOp {
        override fun invoke(from: Long): Long {
            return (deckSize - from - 1)
                .also { println("new stack: $from -> $it")  }
        }
    }

    inner class CutOp(private val cutPos: Long) : ShuffleOp {
        private val c = if (cutPos < 0) cutPos + deckSize else cutPos
        override fun invoke(from: Long): Long {
            return ((deckSize * 2 - c + from) % deckSize)
                .also { println("cut $cutPos: $from -> $it")  }
//            return ((deckSize + c + from) % deckSize)
//                .also { println("cut $cutPos: $from -> $it")  }
        }
    }

    private val invmodCache: MutableMap<Long, Long> = mutableMapOf()

    inner class IncrementOp(private val increment: Long) : ShuffleOp {
        val invmod by lazy {
            invmodCache.computeIfAbsent(increment) { it -> modinv(it, deckSize) } // TODO https://planetcalc.com/3298/
        }

        override fun invoke(from: Long): Long {
//            return ((from * invmod) % deckSize)
//                .also { println("increment $increment: $from -> $it")  }
            return ((from * increment) % deckSize)
                .also { println("increment $increment: $from -> $it")  }

        }
    }
}

fun main() {

    val input = readAllLines("input-2019-22.txt")

    Deck(10007)
        .apply { shuffle(input) }
        .find(2019)
        .also { logWithTime("part 1: $it") }

//    Deck(119315717514047)
//        .apply { shuffle(input, 101741582076661) }
//        .find(2020)
//        .also { logWithTime("part 2: $it") }
}