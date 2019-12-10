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

fun opcode(operation: BigInteger): Int = operation % 100

// params handling
enum class ParamMode { Position, Immediate, Relative }
fun nthParamMode(n: Int, operation: BigInteger): ParamMode {
    var mode = operation / 10
    repeat(n) { mode /= 10 }
    return when (mode % 10) {
        0 -> ParamMode.Position
        1 -> ParamMode.Immediate
        2 -> ParamMode.Relative
        else -> error("Unknown mode in opcode $operation")
    }
}

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

    val operation: BigInteger get() = memory[ip]

    private fun nthAddr(n: Int): BigInteger =
        when (nthParamMode(n, operation)) {
            ParamMode.Position -> memory[ip + n]
            ParamMode.Immediate -> ip + n
            ParamMode.Relative -> rb + memory[ip + n]
        }

    private val firstAddr get() = nthAddr(1)
    private val secondAddr get() = nthAddr(2)
    private val thirdAddr get() = nthAddr(3)

    // main loop
    suspend fun run() {
        while (true) {
//            log("computer $id opcode $opcode at addr $ip")
            when (opcode(operation)) {
                1 -> opADD()
                2 -> opMUL()
                3 -> opIN()
                4 -> opOUT()
                5 -> opJNZ()
                6 -> opJZ()
                7 -> opSETL()
                8 -> opSETE()
                9 -> opMOVRB()
                99 -> {
                    outBuffer.close()
                    return
                }
                else -> error("unknown opcode $operation at addr $ip")
            }
        }
    }

    // operations

    private fun opADD() {
        memory[thirdAddr] = memory[firstAddr] + memory[secondAddr]
        ip += 4
    }

    private fun opMUL() {
        memory[thirdAddr] = memory[firstAddr] * memory[secondAddr]
        ip += 4
    }

    private suspend fun opIN() {
        memory[firstAddr] = read()
        ip += 2
    }

    private suspend fun opOUT() {
        write(memory[firstAddr])
        ip += 2
    }

    private fun opJNZ() {
        ip = when {
            memory[firstAddr] != ZERO -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun opJZ() {
        ip = when {
            memory[firstAddr] == ZERO -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun opSETL() {
        memory[thirdAddr] = if (memory[firstAddr] < memory[secondAddr]) BigInteger.ONE else ZERO
        ip += 4
    }

    private fun opSETE() {
        memory[thirdAddr] = if (memory[firstAddr] == memory[secondAddr]) BigInteger.ONE else ZERO
        ip += 4
    }

    private fun opMOVRB() {
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

operator fun BigInteger.plus(n: Int): BigInteger = this + n.toBigInteger()

operator fun BigInteger.div(i: Int): BigInteger = this / i.toBigInteger()

operator fun BigInteger.rem(i: Int): Int = (this % i.toBigInteger()).toInt()
