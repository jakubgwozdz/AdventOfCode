package advent2019.day17

import org.junit.jupiter.api.Test

internal class Day17KtTest {

    @Test
    internal fun testSeqEq() {
        var s1 = 0
        val seq1 = generateSequence {
            if (s1 > 10) null.also { println("end seq1") }
            else s1++.also { println("seq1: $it") }
        }
        var s2 = 0
        val seq2 = generateSequence {
            if (s2 > 12) null.also { println("end seq2") }
            else s2++.also { if (s2 > 5) s2++ }.also { println("seq2: $it") }
        }

        println(seq1.beginsSameAs(seq2))

    }
}

