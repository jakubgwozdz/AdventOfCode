package advent2019

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class Day03KtTest {

    @Test
    fun part1a() {
        assertThat(calculate("R8,U5,L5,D3", "U7,R6,D4,L4", ::metric1)).isEqualTo(6)
    }

    @Test
    fun part1b() {
        assertThat(
            calculate(
                "R75,D30,R83,U83,L12,D49,R71,U7,L72",
                "U62,R66,U55,R34,D71,R55,D58,R83",
                ::metric1
            )
        ).isEqualTo(159)
    }

    @Test
    fun part1c() {
        assertThat(
            calculate(
                "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51",
                "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
                ::metric1
            )
        ).isEqualTo(135)
    }

    @Test
    fun part2a() {
        assertThat(calculate("R8,U5,L5,D3", "U7,R6,D4,L4", ::metric2)).isEqualTo(30)
    }

    @Test
    fun part2b() {
        assertThat(
            calculate(
                "R75,D30,R83,U83,L12,D49,R71,U7,L72",
                "U62,R66,U55,R34,D71,R55,D58,R83",
                ::metric2
            )
        ).isEqualTo(610)
    }

    @Test
    fun part2c() {
        assertThat(
            calculate(
                "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51",
                "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
                ::metric2
            )
        ).isEqualTo(410)
    }

}

