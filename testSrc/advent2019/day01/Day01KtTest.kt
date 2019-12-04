package advent2019.day01

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

internal class Day01KtTest {

    @Test
    fun testFuel() {
        assertAll(
            { assertEquals(2, fuel(12)) },
            { assertEquals(2, fuel(14)) },
            { assertEquals(654, fuel(1969)) },
            { assertEquals(33583, fuel(100756)) }
        )
    }

    @Test
    fun testFuel2() {
        assertAll(
            { assertEquals(2, fuel2(12)) },
            { assertEquals(2, fuel2(14)) },
            { assertEquals(966, fuel2(1969)) },
            { assertEquals(50346, fuel2(100756)) }
        )
    }
}
