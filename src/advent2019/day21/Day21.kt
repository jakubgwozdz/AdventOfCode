package advent2019.day21

import advent2019.intcode.Intcode
import advent2019.intcode.fullLines
import advent2019.intcode.parseIntcode
import advent2019.intcode.writeln
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

@FlowPreview
fun main() {
    val input = readAllLines("data/input-2019-21.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    goSpring(input, spring1())
        .also { logWithTime("part 1: $it") }

    goSpring(input, spring2())
        .also { logWithTime("part 2: $it") }

}

@FlowPreview
private fun goSpring(input: String, springPrg: List<String>): BigInteger {
    return runBlocking {
        val program = parseIntcode(input)
        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>()
        val computer = Intcode(program, inChannel, outChannel, "ASCII")
        var lastData = BigInteger.ZERO!!

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
                if (it == "Input instructions:") {
                    springPrg.forEach { line -> inChannel.writeln(line) }
                }
            }
            .collect()
        lastData
    }
}

fun spring1(): List<String> {

    /*
     hole: (!A || !B || !C)
     place to land: D

     J = (!A || !B || !C) && D

     */

    return listOf(
        "NOT A J",
        "NOT B T",
        "OR T J",
        "NOT C T",
        "OR T J",
        "AND D J",
        "WALK"
    )
}

fun spring2(): List<String> {

    /*
     hole: (!A || !B || !C)
     place to land: D
     possible move or jump after landing: E || H

     J = (!A || !B || !C) && D && (E || H)

     */

    return listOf(
        "NOT A J",
        "NOT B T",
        "OR T J",
        "NOT C T",
        "OR T J",
        "AND D J",
        "NOT D T",
        "OR E T",
        "OR H T",
        "AND T J",
        "RUN"
    )
}
