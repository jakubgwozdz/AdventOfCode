package advent2019

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class Day01KtTest {

    @Test
    fun testFuel() {
        assertAll(
            { assertThat(fuel(12)).isEqualTo(2) },
            { assertThat(fuel(14)).isEqualTo(2) },
            { assertThat(fuel(1969)).isEqualTo(654) },
            { assertThat(fuel(100756)).isEqualTo(33583) }
        )
    }

    @Test
    fun testFuel2() {
        assertAll(
            { assertThat(fuel2(12)).isEqualTo(2) },
            { assertThat(fuel2(14)).isEqualTo(2) },
            { assertThat(fuel2(1969)).isEqualTo(966) },
            { assertThat(fuel2(100756)).isEqualTo(50346) }
        )
    }
}