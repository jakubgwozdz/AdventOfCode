package advent2019.day18

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day18KtTest {

    @Test
    fun part1Test1() {
        val input = """#########
#b.A.@.a#
#########""".lines()
        expect(8) { moves(input) }
    }

    @Test
    fun part1Test2() {
        val input = """########################
#f.D.E.e.C.b.A.@.a.B.c.#
######################.#
#d.....................#
########################""".lines()
        expect(86) { moves(input) }
    }

    @Test
    fun part1Test3() {
        val input = """########################
#...............b.C.D.f#
#.######################
#.....@.a.B.c.d.A.e.F.g#
########################""".lines()
        expect(132) { moves(input) }
    }

    @Test
    fun part1Test4() {
        val input = """#################
#i.G..c...e..H.p#
########.########
#j.A..b...f..D.o#
########@########
#k.E..a...g..B.n#
########.########
#l.F..d...h..C.m#
#################""".lines()
        expect(136) { moves(input) }
    }

    @Test
    fun part1Test5() {
        val input = """########################
#@..............ac.GI.b#
###d#e#f################
###A#B#C################
###g#h#i################
########################""".lines()
        expect(81) { moves(input) }
    }

    //    @Test
    //    fun testFrom() {
    //        expect(listOf(E,E,W,W,N)) { listOf(1 to 6, 1 to 7, 1 to 6, 1 to 5, 0 to 5).from(1 to 5) }
    //    }
}
