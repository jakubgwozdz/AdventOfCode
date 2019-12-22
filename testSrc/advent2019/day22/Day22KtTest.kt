package advent2019.day22

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day22KtTest {

    val input1 = """deal with increment 7
deal into new stack
deal into new stack""".lines()

    @Test
    fun test1() {
        expect("0 3 6 9 2 5 8 1 4 7") { deal(10, input1).joinToString(" ") }
    }

    val input2 = """cut 6
deal with increment 7
deal into new stack""".lines()

    @Test
    fun test2() {
        expect("3 0 7 4 1 8 5 2 9 6") { deal(10, input2).joinToString(" ") }
    }

    val input3 = """deal with increment 7
deal with increment 9
cut -2""".lines()

    @Test
    fun test3() {
        expect("6 3 0 7 4 1 8 5 2 9") { deal(10, input3).joinToString(" ") }
    }

    val input4 = """deal into new stack
cut -2
deal with increment 7
cut 8
cut -4
deal with increment 7
cut 3
deal with increment 9
deal with increment 3
cut -1""".lines()

    val input4a = input4.drop(1)

    @Test
    fun test4() {
        expect("9 2 5 8 1 4 7 0 3 6") { deal(10, input4).joinToString(" ") }
    }

    @Test
    fun repetitions() {

        (10..100).filter { it % 3 != 0 && it % 7 != 0 }.forEach { deckSize ->
            try {
                val cycle1 = cycle(input1, deckSize)
                val cycle2 = cycle(input2, deckSize)
                val cycle3 = cycle(input3, deckSize)
                val cycle4 = cycle(input4, deckSize)
                val cycle4a = cycle(input4a, deckSize)
                println("$deckSize: $cycle1 $cycle2 $cycle3 $cycle4 $cycle4a")
            } catch (e: Exception) {
                println("$deckSize: $e")
            }
        }

    }

    fun cycle(input: List<String>, deckSize: Int): Int {
        return (1..deckSize).map { it to deal(deckSize, input, it) }
            .first { it.second == deal(deckSize, input, 0) }
            .first
    }


}
