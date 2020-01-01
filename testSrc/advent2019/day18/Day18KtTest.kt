package advent2019.day18

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.expect

internal class Day18KtTest {

    @Test
    fun part1Test1() {
        val input = """
            #########
            #b.A.@.a#
            #########
            """.trimIndent()
        expect(8) { shortest(input.lines()) }
    }

    @Test
    fun part1Test2() {
        val input = """
            ########################
            #f.D.E.e.C.b.A.@.a.B.c.#
            ######################.#
            #d.....................#
            ########################
            """.trimIndent()
        expect(86) { shortest(input.lines()) }
    }

    @Test
    fun part1Test3() {
        val input = """
            ########################
            #...............b.C.D.f#
            #.######################
            #.....@.a.B.c.d.A.e.F.g#
            ########################
            """.trimIndent()
        expect(132) { shortest(input.lines()) }
    }

    @Test
    fun part1Test4() {
        val input = """
            #################
            #i.G..c...e..H.p#
            ########.########
            #j.A..b...f..D.o#
            ########@########
            #k.E..a...g..B.n#
            ########.########
            #l.F..d...h..C.m#
            #################
            """.trimIndent()
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(20)) {
            expect(136) { shortest(input.lines()) }
        }
    }

    @Test
    fun part1Test5() {
        val input = """
            ########################
            #@..............ac.GI.b#
            ###d#e#f################
            ###A#B#C################
            ###g#h#i################
            ########################
            """.trimIndent()
        expect(81) { shortest(input.lines()) }
    }

    //    @Test
    //    fun testFrom() {
    //        expect(listOf(E,E,W,W,N)) { listOf(1 to 6, 1 to 7, 1 to 6, 1 to 5, 0 to 5).from(1 to 5) }
    //    }
}
