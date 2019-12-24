package advent2019.day05

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {

    val programStr = readAllLines("data/input-2019-05.txt").first()

    // part 1
    val output1 = run(programStr, listOf(1))
    logWithTime(output1.toString())

    // part 2
    val output5 = run(programStr, listOf(5))
    logWithTime(output5.toString())

}

fun run(programStr: String, input: List<Int>): List<Int> {
    val program = parse(programStr)
    return loadAndRun(program, input)
}

class Computer(
    val memory: IntArray,
    private val inputOp: () -> Int,
    private val outputOp: (Int) -> Unit
) {
    var ip: Int = 0

    fun read(): Int = inputOp.invoke()
    fun write(v: Int) = outputOp.invoke(v)

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
    fun run() {
        while (true) {
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

    private fun op3() {
        memory[firstParam] = read()
        ip += 2
    }

    private fun op4() {
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

fun loadAndRun(prg: IntArray, input: List<Int> = listOf()): List<Int> {

    val output = mutableListOf<Int>()
    val listIterator = input.listIterator()
    val computer = Computer(prg, { listIterator.next() }, { output.add(it) })

    computer.run()

    return output
}

fun parse(input: String): IntArray {
    return input
        .split(",")
        .map { it.trim().toInt() }
        .toIntArray()
        .also { logWithTime("loaded ${it.size} cells") }
}
