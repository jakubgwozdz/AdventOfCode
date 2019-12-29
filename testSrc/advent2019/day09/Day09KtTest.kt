package advent2019.day09

import advent2019.bi
import advent2019.readAllLines
import org.junit.jupiter.api.Test
import java.math.BigInteger
import kotlin.test.expect

internal class Day09KtTest {

    @Test
    fun sampleQuine() {
        val programStr = "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99"
        val asBigInts = programStr.split(",").map { it.toBigInteger() }
        expect(asBigInts, { run1(programStr, emptyList()) })
    }

    @Test
    fun sample16digits() {
        val programStr = "1102,34915192,34915192,7,4,7,99,0"
        expect(16, { run1(programStr, emptyList()).single().toString().length })
    }

    @Test
    fun sample1125899906842624() {
        val programStr = "104,1125899906842624,99"
        expect(BigInteger("1125899906842624"), { run1(programStr, emptyList()).single() })
    }

    @Test
    fun puzzlePart1() {
        val input = readAllLines("data/input-2019-09.txt").single()
        expect(2351176124.bi) { part1(input) }
    }

    @Test
    fun puzzlePart2() {
        val input = readAllLines("data/input-2019-09.txt").single()
        expect(73110.bi) { part2(input) }
    }



}
