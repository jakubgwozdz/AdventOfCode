package advent2019.day16

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class Day16KtTest {

    @Test
    internal fun testPattern() {
        expect(listOf(1, 0, -1, 0, 1, 0, -1, 0)) { fftPattern(1, 8) }
        expect(listOf(0, 1, 1, 0, 0, -1, -1, 0)) { fftPattern(2, 8) }
    }

    @Test
    internal fun testPhase() {
        expect("48226158") { fftPhase("12345678") }
        expect("34040438") { fftPhase("48226158") }
        expect("03415518") { fftPhase("34040438") }
        expect("01029498") { fftPhase("03415518") }
    }

    @Test
    internal fun testFft() {
        expect("48226158") { fft("12345678", 1) }
        expect("01029498") { fft("12345678", 4) }
        expect("24176176") { fft("80871224585914546619083218645595", 100).take(8) }
        expect("73745418") { fft("19617804207202209144916044189917", 100).take(8) }
        expect("52432133") { fft("69317163492948606335995924319873", 100).take(8) }
    }
}
