package advent2019.day14

import org.junit.jupiter.api.Test
import kotlin.test.expect


internal class Day14KtTest {

    @Test
    internal fun testParsing() {
        val input = """10 ORE => 10 A
1 ORE => 1 B
7 A, 1 B => 1 C
7 A, 1 C => 1 D
7 A, 1 D => 1 E
7 A, 1 E => 1 FUEL"""

        expect(Reaction(listOf(7L to "A", 1L to "E"), 1L to "FUEL")) { parseReaction("7 A, 1 E => 1 FUEL") }

        expect(Reaction(listOf(7L to "A", 1L to "E"), 1L to "FUEL")) { parseReactions(input.lines())["FUEL"] }
        expect(6) { parseReactions(input.lines()).size }

    }

    @Test
    internal fun testNeededOre() {
        val input = """10 ORE => 10 A
1 ORE => 1 B
7 A, 1 B => 1 C
7 A, 1 C => 1 D
7 A, 1 D => 1 E
7 A, 1 E => 1 FUEL"""

        expect(31L) { calculate("ORE", 1L to "FUEL", parseReactions(input.lines())) }
    }

    @Test
    internal fun testMaxFuel() {
        val input = """171 ORE => 8 CNZTR
7 ZLQW, 3 BMBT, 9 XCVML, 26 XMNCP, 1 WPTQ, 2 MZWV, 1 RJRHP => 4 PLWSL
114 ORE => 4 BHXH
14 VRPVC => 6 BMBT
6 BHXH, 18 KTJDG, 12 WPTQ, 7 PLWSL, 31 FHTLT, 37 ZDVW => 1 FUEL
6 WPTQ, 2 BMBT, 8 ZLQW, 18 KTJDG, 1 XMNCP, 6 MZWV, 1 RJRHP => 6 FHTLT
15 XDBXC, 2 LTCX, 1 VRPVC => 6 ZLQW
13 WPTQ, 10 LTCX, 3 RJRHP, 14 XMNCP, 2 MZWV, 1 ZLQW => 1 ZDVW
5 BMBT => 4 WPTQ
189 ORE => 9 KTJDG
1 MZWV, 17 XDBXC, 3 XCVML => 2 XMNCP
12 VRPVC, 27 CNZTR => 2 XDBXC
15 KTJDG, 12 BHXH => 5 XCVML
3 BHXH, 2 VRPVC => 7 MZWV
121 ORE => 7 VRPVC
7 XCVML => 6 RJRHP
5 BHXH, 4 VRPVC => 5 LTCX"""

        expect(2210736L) { calculate("ORE", 1L to "FUEL", parseReactions(input.lines())) }
        expect(460664L ) { calculateMax("FUEL", 1000000000000L to "ORE", parseReactions(input.lines())) }
    }

    @Test
    fun testProductionsCount() {
        expect(1) { productionsCount(1, 1) }
        expect(1) { productionsCount(1, 10) }
        expect(1) { productionsCount(9, 10) }
        expect(1) { productionsCount(10, 10) }
        expect(0) { productionsCount(0, 10) }
        expect(0) { productionsCount(0, 1) }
        expect(11) { productionsCount(11, 1) }
        expect(2) { productionsCount(11, 10) }
        expect(2) { productionsCount(19, 10) }
        expect(2) { productionsCount(20, 10) }
        expect(3) { productionsCount(21, 10) }
    }


}
