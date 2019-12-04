package advent2019.day03

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class Day03KtTest {

    @Test
    fun part1a() {
        assertEquals(6, calculate("R8,U5,L5,D3", "U7,R6,D4,L4", ::metric1))
    }

    @Test
    fun part1b() {
        assertEquals(
            159,
            calculate(
                "R75,D30,R83,U83,L12,D49,R71,U7,L72",
                "U62,R66,U55,R34,D71,R55,D58,R83",
                ::metric1
            )
        )
    }

    @Test
    fun part1c() {
        assertEquals(
            135,
            calculate(
                "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51",
                "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
                ::metric1
            )
        )
    }

    @Test
    fun part2a() {
        assertEquals(30, calculate("R8,U5,L5,D3", "U7,R6,D4,L4", ::metric2))
    }

    @Test
    fun part2b() {
        assertEquals(
            610,
            calculate(
                "R75,D30,R83,U83,L12,D49,R71,U7,L72",
                "U62,R66,U55,R34,D71,R55,D58,R83",
                ::metric2
            )
        )
    }

    @Test
    fun part2c() {
        assertEquals(
            410,
            calculate(
                "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51",
                "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
                ::metric2
            )
        )
    }

}

