package advent2019.day02

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {

    val input = readAllLines("data/input-2019-02.txt").single()
        .also { logWithTime("input length: ${it.length}") }


    // part 1
    part1(input)
        .also { logWithTime("part1: $it") }

    // part 2
    part2(input)
        .also { logWithTime("part2: $it") }
}

fun part1(input: String): Int = run(12, 2, input)[0]

fun part2(input: String): Int = (0..99)
    .flatMap { noun ->
        (0..99).map { verb ->
            val result2 = run(noun, verb, input)
            (noun to verb) to result2
        }
    }
    .single { (input, output) -> output[0] == 19690720 }
    .let { (input, output) -> input}
    .let { (noun, verb) -> 100 * noun + verb }


fun run(noun: Int, verb: Int, input: String): IntArray {
    val program = parse(input)

    program[1] = noun
    program[2] = verb
    process(program)
    return program
}

fun process(prg: IntArray) {
    var i = 0
    while (true) {
//        println("$i: ${prg.toList()}")
        when (prg[i]) {
            1 -> op1(prg, i)
            2 -> op2(prg, i)
            99 -> return
            else -> error("prg[$i] is ${prg[i]}")
        }
        i += 4
    }
}

private fun op2(prg: IntArray, i: Int) {
    prg[prg[i + 3]] = prg[prg[i + 1]] * prg[prg[i + 2]]
}

private fun op1(prg: IntArray, i: Int) {
    prg[prg[i + 3]] = prg[prg[i + 1]] + prg[prg[i + 2]]
}

fun parse(input: String): IntArray {
    return input
        .split(",")
        .map { it.trim().toInt() }
        .toIntArray()
}
