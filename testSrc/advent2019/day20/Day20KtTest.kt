package advent2019.day20

import advent2019.expectSetOf
import advent2019.logWithTime
import advent2019.maze.yx
import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day20KtTest {

    val inputPart1a = """         A           
         A           
  #######.#########  
  #######.........#  
  #######.#######.#  
  #######.#######.#  
  #######.#######.#  
  #####  B    ###.#  
BC...##  C    ###.#  
  ##.##       ###.#  
  ##...DE  F  ###.#  
  #####    G  ###.#  
  #########.#####.#  
DE..#######...###.#  
  #.#########.###.#  
FG..#########.....#  
  ###########.#####  
             Z       
             Z       """.lines()

    @Test
    fun testPart1a() {
        expect(23) { Donut(inputPart1a).shortest().sumBy { it.distance } }
    }

    @Test
    fun testFindPortals() {
        expectSetOf(
            Portal(2 yx 9, "AA"),
            Portal(6 yx 9, "BC"),
            Portal(8 yx 2, "BC"),
            Portal(10 yx 6, "DE"),
            Portal(13 yx 2, "DE"),
            Portal(12 yx 11, "FG"),
            Portal(15 yx 2, "FG"),
            Portal(16 yx 13, "ZZ")
        ) { Donut(inputPart1a).portals }
    }

    @Test
    fun testFindConnection() {
        val aa = Portal(2 yx 9, "AA")
        val bc1 = Portal(6 yx 9, "BC")
        val bc2 = Portal(8 yx 2, "BC")
        val de1 = Portal(10 yx 6, "DE")
        val de2 = Portal(13 yx 2, "DE")
        val fg1 = Portal(12 yx 11, "FG")
        val fg2 = Portal(15 yx 2, "FG")
        val zz = Portal(16 yx 13, "ZZ")
        val e = setOf(
            Connection(bc1,bc2,1),
            Connection(de1,de2,1),
            Connection(fg1,fg2,1),
            Connection(aa, bc1, 4),
            Connection(aa, fg1, 30),
            Connection(bc2, de1, 6),
            Connection(bc1, fg1, 32),
            Connection(bc1, zz, 28),
            Connection(de2, fg2, 4),
            Connection(fg1, zz, 6),
            Connection(aa, zz, 26)
        )
            .let { s -> s + s.map { c -> Connection(c.portal2, c.portal1, c.distance) } }
            .sorted()
        val a = Donut(inputPart1a).roads.toSet().sorted()
        logWithTime("e: $e")
        logWithTime("a: $a")
        expect(e) { a }
    }

    val inputPart1b = """                   A               
                   A               
  #################.#############  
  #.#...#...................#.#.#  
  #.#.#.###.###.###.#########.#.#  
  #.#.#.......#...#.....#.#.#...#  
  #.#########.###.#####.#.#.###.#  
  #.............#.#.....#.......#  
  ###.###########.###.#####.#.#.#  
  #.....#        A   C    #.#.#.#  
  #######        S   P    #####.#  
  #.#...#                 #......VT
  #.#.#.#                 #.#####  
  #...#.#               YN....#.#  
  #.###.#                 #####.#  
DI....#.#                 #.....#  
  #####.#                 #.###.#  
ZZ......#               QG....#..AS
  ###.###                 #######  
JO..#.#.#                 #.....#  
  #.#.#.#                 ###.#.#  
  #...#..DI             BU....#..LF
  #####.#                 #.#####  
YN......#               VT..#....QG
  #.###.#                 #.###.#  
  #.#...#                 #.....#  
  ###.###    J L     J    #.#.###  
  #.....#    O F     P    #.#...#  
  #.###.#####.#.#####.#####.###.#  
  #...#.#.#...#.....#.....#.#...#  
  #.#####.###.###.#.#.#########.#  
  #...#.#.....#...#.#.#.#.....#.#  
  #.###.#####.###.###.#.#.#######  
  #.#.........#...#.............#  
  #########.###.###.#############  
           B   J   C               
           U   P   P               """.lines()

    @Test
    fun testPart1b() {
        expect(58) { Donut(inputPart1b).shortest().sumBy { it.distance } }
    }


}
