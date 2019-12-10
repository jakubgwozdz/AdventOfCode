package advent2019.day10

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.expect

internal class Day10KtTest {
    @Test
    fun testParseAsteroids() {

        val lines = """
            |.#..#
            |.....
            |#####
            |....#
            |...##
            """.trimMargin().lines()

        expect(5) { lines.size }

        val asteroids = parseAsteroids(lines)

        expect(10) { asteroids.size }

        expect(setOf(0 to 1, 0 to 4, 2 to 0, 2 to 1, 2 to 2, 2 to 3, 2 to 4, 3 to 4, 4 to 3, 4 to 4)) {
            asteroids.toSet()
        }
    }

    @Test
    fun testFindBestLocation() {
        val lines = """
            |.#..#
            |.....
            |#####
            |....#
            |...##
            """.trimMargin().lines()
        val asteroids = parseAsteroids(lines)

        assertEquals((4 to 3) to 8, findBestLocation(asteroids).run { first to second.size })
    }

    @Test
    fun testStep() {
        assertAll(
            { assertEquals(-1 to 0, (-8 to 0).step) },
            { assertEquals(1 to 1, (1 to 1).step) },
            { assertEquals(0 to -1, (0 to -1).step) },
            { assertEquals(1 to 0, (5 to 0).step) },
            { assertEquals(1 to 1, (5 to 5).step) },
            { assertEquals(1 to 2, (1 to 2).step) },
            { assertEquals(1 to -2, (1 to -2).step) },
            { assertEquals(1 to 2, (4 to 8).step) },
            { assertEquals(3 to 2, (6 to 4).step) },
            { assertEquals(3 to 2, (3 to 2).step) },
            { assertEquals(4 to 3, (16 to 12).step) },
            { assertEquals(4 to -3, (16 to -12).step) },
            { assertEquals(-4 to -3, (-16 to -12).step) },
            { assertEquals(-4 to 3, (-16 to 12).step) },
            { assertEquals(3 to 4, (12 to 16).step) },
            { assertEquals(3 to -4, (12 to -16).step) },
            { assertEquals(-3 to -4, (-12 to -16).step) },
            { assertEquals(-3 to 4, (-12 to 16).step) },
            { assertEquals(0 to 0, (0 to 0).step) }
        )
    }

    @Test
    fun testAngle() {
//        assertAll(
//            { assertTrue { (-8 to 0).angle.let { it > } } },
//            { expect(0.0) { (-5 to 0).angle } }
//        )
        listOf(
            -10 to 0, -10 to 1, -10 to 10, -1 to 10, 0 to 10, 1 to 10, 10 to 10, 10 to 1,
            10 to 0, 10 to -1, 10 to -10, 1 to -10, 0 to -10, -1 to -10, -10 to -10, -10 to -1
        )
            .forEach { println("$it.angle = ${it.angle}") }
    }


}
