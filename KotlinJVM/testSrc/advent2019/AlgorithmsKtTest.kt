package advent2019

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class AlgorithmsKtTest {

    @Test
    fun gcd() {
        expect(6) { gcd(24, 30) }
        expect(1) { gcd(3, 5) }
    }

    @Test
    fun lcm() {
        expect(120) { lcm(24, 30) }
        expect(15) { lcm(3, 5) }
    }
}