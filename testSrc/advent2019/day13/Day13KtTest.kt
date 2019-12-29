package advent2019.day13

import advent2019.readAllLines
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day13KtTest {
    @Test
    fun puzzlePart1() {
        val input = readAllLines("data/input-2019-13.txt").single()
        expect(265) { part1(input) }
    }

    @Test
    fun puzzlePart2() {
        val input = readAllLines("data/input-2019-13.txt").single()
        expect(13331) { part2(input) }
    }

}
