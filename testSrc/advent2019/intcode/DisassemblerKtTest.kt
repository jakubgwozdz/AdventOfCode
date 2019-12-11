package advent2019.intcode

import advent2019.logWithTime
import advent2019.readAllLines
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigInteger.ZERO

internal class DisassemblerKtTest {
    @Test
    fun day9() {
        val programStr = readAllLines("input-2019-09.txt").first()
        val memory = parse(programStr)
        var addr = ZERO
        while (addr < memory.size) {
            dissassembly(memory, addr)
                .also {
                    println("${addr.toString().padStart(6)}: $it")
                    addr += it.size
                }
        }

    }
}