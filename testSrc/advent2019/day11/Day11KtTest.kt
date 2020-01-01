package advent2019.day11

import advent2019.readAllLines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import kotlin.test.expect

@ExperimentalCoroutinesApi
internal class Day11KtTest {

    @Test
    fun puzzlePart1() {
        val input = readAllLines("data/input-2019-11.txt").single()
        expect(1967) { part1(input) }
    }

}
