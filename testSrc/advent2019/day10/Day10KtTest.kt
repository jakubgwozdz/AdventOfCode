package advent2019.day10

import advent2019.AssertBuilder
import advent2019.assertThat
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
    fun testAngle() {
        listOf(
            -10 to 0, -10 to 1, -10 to 10, -1 to 10, 0 to 10, 1 to 10, 10 to 10, 10 to 1,
            10 to 0, 10 to -1, 10 to -10, 1 to -10, 0 to -10, -1 to -10, -10 to -10, -10 to -1
        ).run {
            this.indices
                .filter { index -> index > 0 }
                .forEach { index -> assertThat(this[index]).hasGreaterAngleThan(this[index - 1]) }
        }
    }

    @Test
    fun testBlocks() {
        assertAll(
            { assertThat(2 to 2).blocksNot(-5 to -5) },
            { assertThat(0 to 2).blocksNot(0 to -5) },
            { assertThat(0 to 2).blocksNot(0 to 1) },
            { assertThat(0 to 1).blocks(0 to 2) },
            { assertThat(-12 to -9).blocksNot(16 to 12) },
            { assertThat(-12 to 9).blocksNot(16 to 12) },
            { assertThat(12 to -9).blocksNot(16 to 12) },
            { assertThat(9 to 12).blocksNot(16 to 12) },
            { assertThat(12 to 9).blocks(16 to 12) },
            { assertThat(16 to 12).blocksNot(8 to 6) },
            { assertThat(4 to 3).blocks(16 to 12) },
            { assertThat(1 to 1).blocksNot(1 to 1) }
        )
    }

}

private fun AssertBuilder<Vector>.hasGreaterAngleThan(o: Vector) =
    isTrue("$t (angle ${t.angle}) should have greater angle than $o (angle ${o.angle})") { it.angle > o.angle }

private fun AssertBuilder<Vector>.blocks(o: Vector) = isTrue("$t should block $o") { it.blocks(o) }
private fun AssertBuilder<Vector>.blocksNot(o: Vector) = isFalse("$t should NOT block $o") { it.blocks(o) }