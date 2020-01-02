package advent2019.day16

import advent2019.readAllLines
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.expect

internal class Day16KtTest {

    @Test
    fun testPhase() {
        expect("48226158") { fftPhase("12345678") }
        expect("34040438") { fftPhase("48226158") }
        expect("03415518") { fftPhase("34040438") }
        expect("01029498") { fftPhase("03415518") }
    }

    private fun fftPhase(input: String): String {
        return fftPhase(input.toDigits()).asString()
    }

    @Test
    fun testFft() {
        expect("48226158") { fft("12345678", 1) }
        expect("01029498") { fft("12345678", 4) }
    }

    @Test
    fun testPart1() {
        expect("24176176") { fft("80871224585914546619083218645595", 100,0,8) }
        expect("73745418") { fft("19617804207202209144916044189917", 100,0,8) }
        expect("52432133") { fft("69317163492948606335995924319873", 100,0,8) }
    }

    @Test
    fun testPart2a() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10)) {
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
    @Disabled
    fun testPart2b() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(10)) {
            expect("84462026") { fftRepeat("03036732577212944063491565474664", 10000, 100, 303673, 8) }
        }
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("78725270") { fftRepeat("02935109699940807407585447034323", 10000, 100, 293510, 8) }
        }
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(3)) {
            expect("53553731") { fftRepeat("03081770884921959731165446850517", 10000, 100, 308177, 8) }
        }
    }

    @Test
    fun testRepeated() {
        expect("34389886592132718868384101444361") {
            fftRepeat("69317163492948616931716349294861", 1, 10, 0, 32)
        }
        expect("34389886592132718868384101444361") {
            fftRepeat("6931716349294861", 2, 10, 0, 32)
        }
    }

    @Test
    fun testRepeated2() {
        expect("542132465648252") {
            fftRepeat("432524325243252", 1, 10, 0, 15)
        }
        expect("542132465648252") {
            fftRepeat("43252", 3, 10, 0, 15)
        }
    }

    @Test
    fun testRepeated3a() {
        expect("403893683670591085908457663043256980782088917468785168225711620724036") {
            fftRepeat("432528237465328745290364325282374653287452903643252823746532874529036", 1, 10, 0, 69)
        }
    }
    @Test
    fun testRepeated3b() {
        expect("403893683670591085908457663043256980782088917468785168225711620724036") {
            fftRepeat("43252823746532874529036", 3, 10, 0, 69)
        }
    }

    @Test
    @Disabled
    fun testRepeated4() {
        expect("11111111") {
            fftRepeat("1111", 10000, 100, 0, 100)
        }
    }

    @Test
    fun testPart1Input() {
        expect("73127523") {
            fft(readAllLines("data/input-2019-16.txt").single(), 100).take(8)
        }
    }

}
