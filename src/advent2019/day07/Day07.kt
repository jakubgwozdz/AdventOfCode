package advent2019.day07

import advent2019.logWithTime
import advent2019.permutations
import advent2019.readFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {

    val programStr = readFile("input-2019-07.txt").first()
        .also { logWithTime("Program length (chars): ${it.length}") }

    // part 1
    val permutations = permutations(5)

    permutations
        .map { it to run(programStr, it) }
        .maxBy { it.second }
        .also { logWithTime("task1: $it") }

    permutations
        .map { it.map { i -> i + 5 } }
        .map { it to run2(programStr, it) }
        .maxBy { it.second }
        .also { logWithTime("task2: $it") }
}

fun run(programStr: String, input: List<Int>): Int {
    var output = 0
    input.forEachIndexed { index, ampl ->
        val program = parse(programStr)
        val inputBuffer = Buffer<Int>('A' + index)
        val outputBuffer = Buffer<Int>('a' + index)
        val computer = Computer('A' + index, program, inputBuffer, outputBuffer)
        output = runBlocking {
            launch { computer.run() }
            inputBuffer.apply {
                put(ampl)
                put(output)
            }
            outputBuffer.take()
        }
    }
    return output
}

class Buffer<T>(val id: Any) {
    private val queue = Channel<T>(Channel.UNLIMITED)
    suspend fun put(e: T) = queue.send(e)
    suspend fun take(): T = queue.receive()
}

fun run2(programStr: String, input: List<Int>): Int {

    val buffers = input.indices
        .map { 'A' + it }
        .map { Buffer<Int>(it) }

    val computers = input.indices
        .map { 'A' + it }
        .mapIndexed { index, id ->
            Computer(
                id,
                parse(programStr),
                buffers[index],
                buffers[(index + 1) % buffers.size]
            )
        }

    runBlocking {
        computers.map {
            launch {
                it.run()
            }
        }

        buffers.forEachIndexed { index, buffer ->
            buffer.put(input[index])
        }

        buffers[0].put(0)
    }

    return runBlocking {
        buffers[0].take()
    }
}

class Computer(
    val id: Any,
    val memory: IntArray,
    private val inputBuffer: Buffer<Int>,
    private val outputBuffer: Buffer<Int>
) {
    var ip: Int = 0

    suspend fun read(): Int = inputBuffer.take()
    suspend fun write(v: Int) = outputBuffer.put(v)

    val opcode get() = memory[ip]

    // params handling
    private fun nthParam(n: Int): Int {
        var mode = opcode / 10
        repeat(n) { mode /= 10 }
        mode %= 10
        return when (mode) {
            0 -> memory[ip + n]
            1 -> ip + n
            else -> error("Unknown mode in opcode $opcode at addr $ip")
        }
    }

    private val firstParam get() = nthParam(1)
    private val secondParam get() = nthParam(2)
    private val thirdParam get() = nthParam(3)
    private val fourthParam get() = nthParam(4)

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
                99 -> return
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
            memory[firstParam] != 0 -> memory[secondParam]
            else -> ip + 3
        }
    }

    private fun op6() {
        ip = when {
            memory[firstParam] == 0 -> memory[secondParam]
            else -> ip + 3
        }
    }

    private fun op7() {
        memory[thirdParam] = if (memory[firstParam] < memory[secondParam]) 1 else 0
        ip += 4
    }

    private fun op8() {
        memory[thirdParam] = if (memory[firstParam] == memory[secondParam]) 1 else 0
        ip += 4
    }

}

fun parse(input: String): IntArray {
    return input
        .split(",")
        .map { it.trim().toInt() }
        .toIntArray()
}
