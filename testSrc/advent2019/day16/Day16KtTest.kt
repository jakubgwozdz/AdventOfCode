package advent2019.day16

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.expect

internal class Day16KtTest {

    @Test
    internal fun testPattern() {
        expect(listOf(1, 0, -1, 0, 1, 0, -1, 0)) { fftPattern(1, 8) }
        expect(listOf(0, 1, 1, 0, 0, -1, -1, 0)) { fftPattern(2, 8) }
        expect(listOf(0, 0, 1, 1, 1, 0, 0, 0)) { fftPattern(3, 8) }
    }

    private fun fftPattern(i: Int, l: Int): List<Int> {
        return (1..l).map { fftPatternSingle(it, i) }
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
    }

    @Test
    internal fun testPart1() {
        expect("24176176") { fft("80871224585914546619083218645595", 100).take(8) }
        expect("73745418") { fft("19617804207202209144916044189917", 100).take(8) }
        expect("52432133") { fft("69317163492948606335995924319873", 100).take(8) }
    }

    @Test
    internal fun testPart2a() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("24176176") { fftRepeat("80871224585914546619083218645595", 1, 100, 0, 8) }
        }
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("73745418") { fftRepeat("19617804207202209144916044189917", 1, 100, 0, 8) }
        }
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("52432133") { fftRepeat("69317163492948606335995924319873", 1, 100, 0, 8) }
        }
    }

    @Test
    internal fun testPart2b() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("84462026") { fftRepeat("03036732577212944063491565474664", 10000, 100, 303673, 8) }
        }
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("78725270") { fftRepeat("02935109699940807407585447034323", 10000, 100, 293510, 8) }
        }
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("53553731") { fftRepeat("03081770884921959731165446850517", 10000, 100, 308177, 8) }
        }
    }

}
