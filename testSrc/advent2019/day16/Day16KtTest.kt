package advent2019.day16

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day16KtTest {
    @Test
    internal fun testPhase() {
        expect("48226158") { fftPhase("12345678") }
        expect("34040438") { fftPhase("48226158") }
        expect("03415518") { fftPhase("34040438") }
        expect("01029498") { fftPhase("03415518") }
    }

    @Test
    internal fun testPattern() {
        expect(listOf(1, 0, -1, 0, 1, 0 - 1, 0)) { fftPattern(1, 8) }
        expect(listOf(0, 1, 1, 0, 0, -1, -1, 0)) { fftPattern(2, 8) }
    }
}
