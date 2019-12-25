package advent2019.day17

import advent2019.intcode.*
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.math.BigInteger.ZERO
import kotlin.system.measureTimeMillis

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalStdlibApi
fun main() {
    measureTimeMillis {
        val input = readAllLines("data/input-2019-17.txt").single()
            .also { logWithTime("Program length (chars): ${it.length}") }

        val program1 = parseIntcode(input)
        val scaffolding = Scaffolding(drawMap(program1).also { l -> l.forEach { logWithTime(it) } })

        alignment(scaffolding)
            .also { logWithTime("part 1: $it") }

        val (mainRoutine, functions) = findRoutine(scaffolding)
        logWithTime("mainRoutine: $mainRoutine")
        val (functionA, functionB, functionC) = functions
        logWithTime("functionA: $functionA")
        logWithTime("functionB: $functionB")
        logWithTime("functionC: $functionC")

        val program2 = parseIntcode(input).also { it[0.toBigInteger()] = 2.toBigInteger() }

        moveRobot(program2, mainRoutine, functionA, functionB, functionC)
            .also { logWithTime("part 2: $it") }
    }.also { println("${it}ms") }
}


private fun alignment(scaffolding: Scaffolding): Int =
    scaffolding.innerIndices
        .filter { pos -> scaffolding.hasIntersection(pos) }
        .map { (row, column) -> row * column }
        .sum()

@FlowPreview
@ExperimentalStdlibApi
private fun drawMap(program: Memory): List<String> = runBlocking {
    val inChannel = Channel<BigInteger>()
    val outChannel = Channel<BigInteger>()
    val computer = Intcode(program, inChannel, outChannel, "ASCII")
    launch {
        computer.run()
        inChannel.close()
        outChannel.close()
    }
    outChannel.consumeAsFlow()
        .map { it.toInt().toChar() }
        .fullLines()
        .toList()
}

@ExperimentalCoroutinesApi
@FlowPreview
private fun moveRobot(
    program: Memory,
    mainRoutine: String,
    functionA: String,
    functionB: String,
    functionC: String
): BigInteger {
    return runBlocking {
        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>()
        val computer = Intcode(program, inChannel, outChannel, "ASCII")
        var lastData = ZERO!!
        launch {
            computer.run()
            inChannel.close()
            outChannel.close()
        }
        outChannel.consumeAsFlow()
            .onEach { lastData = it }
            .map { it.toInt().toChar() }
            .fullLines()
            .onEach {
                println(it)
                if (it == "Main:") inChannel.writeln(mainRoutine)
                if (it == "Function A:") inChannel.writeln(functionA)
                if (it == "Function B:") inChannel.writeln(functionB)
                if (it == "Function C:") inChannel.writeln(functionC)
                if (it == "Continuous video feed?") inChannel.writeln("n")
            }
            .collect()
            .let { lastData }
    }
}

