package advent2019.intcode

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.math.BigInteger
import java.math.BigInteger.ZERO

class Memory(initial: Map<BigInteger, BigInteger>) {
    val map = initial.toMutableMap()
    operator fun get(addr: BigInteger) = map[addr] ?: ZERO
    operator fun set(addr: BigInteger, value: BigInteger) {
        map[addr] = value
    }
}

sealed class Param {
    abstract val value: BigInteger
}

data class PositionParam(override val value: BigInteger): Param()
data class ImmediateParam(override val value: BigInteger): Param()
data class RelativeParam(override val value: BigInteger): Param()

data class Instruction(val opcode: BigInteger, val params:List<Param>)

class Computer(
    val id: Any,
    val memory: Memory,
    val inBuffer: ReceiveChannel<BigInteger>,
    val outBuffer: SendChannel<BigInteger>
) {
    var ip: BigInteger = ZERO // instruction pointer
    var rb: BigInteger = ZERO // relative base

    suspend fun read(): BigInteger = inBuffer.receive()
    suspend fun write(v: BigInteger) = outBuffer.send(v)

    val opcode get() = memory[ip]

    // params handling
    private fun nthParam(n: Int): BigInteger {
        var mode = opcode / 10
        repeat(n) { mode /= 10 }

        val pos = ip + n
        return when (mode % 10) {
            0 -> memory[pos]
            1 -> pos
            2 -> rb + memory[pos]
            else -> error("Unknown mode in opcode $opcode at addr $ip")
        }
    }

    private val firstAddr get() = nthParam(1)
    private val secondAddr get() = nthParam(2)
    private val thirdAddr get() = nthParam(3)

    // main loop
    suspend fun run() {
        while (true) {
//            log("computer $id opcode $opcode at addr $ip")
            when (opcode % 100) {
                1 -> op1()
                2 -> op2()
                3 -> op3()
                4 -> op4()
                5 -> op5()
                6 -> op6()
                7 -> op7()
                8 -> op8()
                9 -> op9()
                99 -> {
                    outBuffer.close()
                    return
                }
                else -> error("unknown opcode $opcode at addr $ip")
            }
        }
    }

    // operations

    private fun op1() {
        memory[thirdAddr] = memory[firstAddr] + memory[secondAddr]
        ip += 4
    }

    private fun op2() {
        memory[thirdAddr] = memory[firstAddr] * memory[secondAddr]
        ip += 4
    }

    private suspend fun op3() {
        memory[firstAddr] = read()
        ip += 2
    }

    private suspend fun op4() {
        write(memory[firstAddr])
        ip += 2
    }

    private fun op5() {
        ip = when {
            memory[firstAddr] != ZERO -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun op6() {
        ip = when {
            memory[firstAddr] == ZERO -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun op7() {
        memory[thirdAddr] = if (memory[firstAddr] < memory[secondAddr]) BigInteger.ONE else ZERO
        ip += 4
    }

    private fun op8() {
        memory[thirdAddr] = if (memory[firstAddr] == memory[secondAddr]) BigInteger.ONE else ZERO
        ip += 4
    }

    private fun op9() {
        rb += memory[firstAddr]
        ip += 2
    }

}

fun parse(input: String): Memory {
    return input
        .split(",")
        .mapIndexed { index: Int, s: String -> index.toBigInteger() to s.trim().toBigInteger() }
        .toMap()
        .let { Memory(it) }
}

private operator fun BigInteger.plus(n: Int): BigInteger = this + n.toBigInteger()

private operator fun BigInteger.div(i: Int): BigInteger = this / i.toBigInteger()

private operator fun BigInteger.rem(i: Int): Int = (this % i.toBigInteger()).toInt()
