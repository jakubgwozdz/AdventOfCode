package advent2019.day24

import advent2019.readAllLines
import org.junit.jupiter.api.Test
import kotlin.test.expect

@ExperimentalStdlibApi
internal class Day24KtTest {
    @Test
    fun puzzlePart1() {
        val input = readAllLines("data/input-2019-24.txt")
        expect(23967691) { part1(input) }
    }

    @Test
    fun puzzlePart2() {
        val input = readAllLines("data/input-2019-24.txt")
        expect(2003) { part2(input) }
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
        expect(Eris(next)) { Eris(example).nextSinglePlane() }
    }

    @Test
    fun example1Alives() {
        val example = """
            ....#
            #..#.
            #..##
            ..#..
            #....
            """.trimIndent().lines()
        expect(10) {
            var eris = Eris(example)
            repeat(4) { eris = eris.nextSinglePlane() }
            eris.alives
        }
    }

    @Test
    fun example2Alives() {
        val example = """
            ....#
            #..#.
            #..##
            ..#..
            #....
            """.trimIndent().lines()
        expect(99) {
            var eris = Eris(example)
            repeat(10) { eris = eris.nextMultiPlane() }
            eris.alives
        }
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
