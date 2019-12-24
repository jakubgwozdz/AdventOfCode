package advent2019.intcode

import advent2019.readAllLines
import org.junit.jupiter.api.Test
import java.math.BigInteger.ZERO

internal class DisassemblerKtTest {
    @Test
    fun day11() {
        val programStr = readAllLines("data/input-2019-15.txt").first()
        disassemblyProgram(programStr).forEach { println(it) }
    }
}
