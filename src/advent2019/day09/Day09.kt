package advent2019.day09

import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

fun main() {

    val programStr = readAllLines("input-2019-09.txt").first()
        .also { logWithTime("Program length (chars): ${it.length}") }

    run1(programStr, listOf(ONE))
        .also { logWithTime("part1: $it") }

    run1(programStr, listOf(2.toBigInteger()))
        .also { logWithTime("part2: $it") }
}

fun run1(programStr: String, input: List<BigInteger>): List<BigInteger> {

    val computer = Computer("BOOST", parse(programStr))

    runBlocking {
        launch {
            computer.run()
        }

        input.forEach { computer.inBuffer.send(it) }
    }

    return runBlocking {
        computer.outBuffer.toList()
    }
}

class Buffer(val id: Any, private val channel: Channel<BigInteger> = Channel(Channel.UNLIMITED)) :
    Channel<BigInteger> by channel

class Memory(initial: Map<Int, BigInteger>) {
    val map = initial.toMutableMap()
    operator fun get(addr: Int) = map[addr] ?: ZERO
    operator fun set(addr: Int, value: BigInteger) {
        map[addr] = value
    }
}

class Computer(
    val id: Any,
    val memory: Memory,
    val inBuffer: Buffer = Buffer("->$id"),
    val outBuffer: Buffer = Buffer("$id->")
) {
    var ip: Int = 0 // instruction pointer
    var rb: Int = 0 // relative base

    suspend fun read(): BigInteger = inBuffer.receive()
    suspend fun write(v: BigInteger) = outBuffer.send(v)

    val opcode get() = memory[ip].toInt()

    // params handling
    private fun nthParam(n: Int): Int {
        var mode = opcode / 10
        repeat(n) { mode /= 10 }
        mode %= 10
        return when (mode) {
            0 -> memory[ip + n].toInt()
            1 -> ip + n
            2 -> rb + memory[ip + n].toInt()
            else -> error("Unknown mode in opcode $opcode at addr $ip")
        }
    }

    private val firstParam get() = nthParam(1)
    private val secondParam get() = nthParam(2)
    private val thirdParam get() = nthParam(3)

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
        memory[thirdParam] = memory[firstParam] + memory[secondParam]
        ip += 4
    }

    private fun op2() {
        memory[thirdParam] = memory[firstParam] * memory[secondParam]
        ip += 4
    }

    private suspend fun op3() {
        memory[firstParam] = read()
        ip += 2
    }

    private suspend fun op4() {
        write(memory[firstParam])
        ip += 2
    }

    private fun op5() {
        ip = when {
            memory[firstParam] != ZERO -> memory[secondParam].toInt()
            else -> ip + 3
        }
    }

    private fun op6() {
        ip = when {
            memory[firstParam] == ZERO -> memory[secondParam].toInt()
            else -> ip + 3
        }
    }

    private fun op7() {
        memory[thirdParam] = if (memory[firstParam] < memory[secondParam]) ONE else ZERO
        ip += 4
    }

    private fun op8() {
        memory[thirdParam] = if (memory[firstParam] == memory[secondParam]) ONE else ZERO
        ip += 4
    }

    private fun op9() {
        rb += memory[firstParam].toInt()
        ip += 2
    }

}

fun parse(input: String): Memory {
    return input
        .split(",")
        .mapIndexed { index: Int, s: String -> index to s.trim().toBigInteger() }
        .toMap()
        .let { Memory(it) }
}
