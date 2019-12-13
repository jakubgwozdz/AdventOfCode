package advent2019.day05

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

internal class Day05KtTest {

    @Test
    fun samplesDay02() {
        assertAll(
            { assertEquals(3500, parse("1,9,10,3,2,3,11,0,99,30,40,50").also { loadAndRun(it) }[0]) },
            { assertEquals(2, parse("1,0,0,0,99").also { loadAndRun(it) }[0]) },
            { assertEquals(2, parse("2,3,0,3,99").also { loadAndRun(it) }[0]) },
            { assertEquals(30, parse("1,1,1,4,99,5,6,0,99").also { loadAndRun(it) }[0]) }
        )
    }

    @Test
    fun samplesDay05() {
        assertAll(
            { assertEquals(1002, parse("1002,4,3,4,33").also { loadAndRun(it) }[0]) },
            { assertEquals(1101, parse("1101,100,-1,4,0").also { loadAndRun(it) }[0]) },
            { assertEquals(99, parse("99").also { loadAndRun(it) }[0]) }
        )
    }

}
