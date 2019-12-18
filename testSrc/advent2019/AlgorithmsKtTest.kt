package advent2019

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class AlgorithmsKtTest {

    @Test
    fun testPermutationsWithRepetitions() {
        expect(listOf("0,0", "1,0", "2,0", "0,1", "1,1", "2,1", "0,2", "1,2", "2,2")) {
            permutationsWithRepetitions(3, 2).map { it.joinToString(",") }.toList()
        }
    }

    @Test
    fun testPermutations() {
        expect(listOf("0,1,2", "0,2,1", "1,0,2", "1,2,0", "2,0,1", "2,1,0")) {
            permutations(3).map { it.joinToString(",") }.toList()
        }
    }
}
