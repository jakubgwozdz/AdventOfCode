package advent2019.day02

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

internal class Day02KtTest {

    @Test
    fun testParse() {
        val input = "1,9,10,3,2,3,11,0,99,30,40,50\n"
        val program = parse(input)
        assertEquals(listOf(1, 9, 10, 3, 2, 3, 11, 0, 99, 30, 40, 50), program.asList())
    }

    @Test
    fun samples() {
        assertAll(
            { assertEquals(3500, parse("1,9,10,3,2,3,11,0,99,30,40,50").also { process(it) }[0]) },
            { assertEquals(2, parse("1,0,0,0,99").also { process(it) }[0]) },
            { assertEquals(2, parse("2,3,0,3,99").also { process(it) }[0]) },
            { assertEquals(30, parse("1,1,1,4,99,5,6,0,99").also { process(it) }[0]) }
        )
    }

}
