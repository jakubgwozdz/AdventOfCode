package advent2019.day24

import advent2019.readAllLines
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day24KtTest {
    @Test
    fun puzzlePart1() {
        val input = readAllLines("data/input-2019-24.txt")
        expect(23967691) { part1(input) }
    }

    @Test
    fun puzzlePart2() {
        val input = readAllLines("data/input-2019-24.txt")
        expect(13331) { part2(input) }
    }

    @Test
    fun example1() {
        val example = """
            ....#
            #..#.
            #..##
            ..#..
            #....
            """.trimIndent().lines()
        val next = """
            #..#.
            ####.
            ###.#
            ##.##
            .##..
            """.trimIndent().lines()
        expect(Eris(next)) { Eris(example).next() }
    }

    @Test
    fun biodiversity() {
        val example = """
            .....
            .....
            .....
            #....
            .#...
            """.trimIndent().lines()
        expect(2129920) { Eris(example).biodiversity }
    }

}
