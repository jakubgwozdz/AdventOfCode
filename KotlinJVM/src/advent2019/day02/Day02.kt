package advent2019.day02

import java.nio.file.Files
import java.nio.file.Paths

fun main() {

    // part 1
    val result = run(12, 2)
    println(result[0])

    // part 2
    (0..99).forEach { noun ->
        (0..99).forEach { verb ->
            val result2 = run(noun, verb)
//            if (noun == 12 && verb == 2) println("${100 * noun + verb}: ${result2[0]}")
            if (result2[0] == 19690720) println("${100 * noun + verb}: ${result2[0]}")
        }

    }

}

fun run(noun: Int, verb: Int): IntArray {
    val input = Files.readString(Paths.get("input-2019-02.txt"))
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
