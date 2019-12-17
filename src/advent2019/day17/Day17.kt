package advent2019.day17

import advent2019.day17.Direction.*
import advent2019.intcode.Computer
import advent2019.intcode.Memory
import advent2019.intcode.parse
import advent2019.logWithTime
import advent2019.oneOfOrNull
import advent2019.permutationsWithRepetitions
import advent2019.readAllLines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.math.BigInteger.ZERO

@ExperimentalCoroutinesApi
@FlowPreview
@ExperimentalStdlibApi
fun main() {
    val input = readAllLines("input-2019-17.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    val program1 = parse(input)
    val scaffolding = Scaffolding(drawMap(program1))

    alignment(scaffolding)
        .also { logWithTime("part 1: $it") }

    val (mainRoutine, functions) = findRoutine(scaffolding)
    logWithTime("mainRoutine: $mainRoutine")
    val (functionA, functionB, functionC) = functions
    logWithTime("functionA: $functionA")
    logWithTime("functionB: $functionB")
    logWithTime("functionC: $functionC")

    val program2 = parse(input).also { it[0.toBigInteger()] = 2.toBigInteger() }

    moveRobot(program2, mainRoutine, functionA, functionB, functionC)
        .also { logWithTime("part 2: $it") }
}

enum class Direction(val char: Char, val change: Pair<Int, Int>) {
    UP('^', -1 to 0),
    DOWN('v', 1 to 0),
    LEFT('<', 0 to -1),
    RIGHT('>', 0 to 1)
}

typealias Position = Pair<Int, Int>

operator fun Position.plus(d: Direction) = first + d.change.first to second + d.change.second

class Scaffolding(val map: List<String>) {
    operator fun get(pos: Position): Char {
        val (y, x) = pos
        return if (y !in map.indices || x !in map[y].indices) '.'
        else map[y][x]
    }

    fun hasIntersection(pos: Position): Boolean =
        hasPath(pos) && Direction.values().all { hasPath(pos + it) }

    fun hasPath(pos: Position) = this[pos] == '#'

    val robot
        get() = map.allIndices.single { (y, x) -> map[y][x] != '#' && map[y][x] != '.' }
}

typealias Movement = List<Pair<Char, Int>>

// Naive but fortunately enough for the puzzle
fun findPath(scaffolding: Scaffolding): Movement {
    var robot = scaffolding.robot
    var direction: Direction? = Direction.values().single { it.char == scaffolding[robot] }

    val result = mutableListOf<Pair<Char, Int>>()
    while (direction != null) {
        val turn = when (direction) {
            UP -> oneOfOrNull('L' to LEFT, 'R' to RIGHT) { scaffolding.hasPath(robot + it.second) }
            RIGHT -> oneOfOrNull('L' to UP, 'R' to DOWN) { scaffolding.hasPath(robot + it.second) }
            DOWN -> oneOfOrNull('L' to RIGHT, 'R' to LEFT) { scaffolding.hasPath(robot + it.second) }
            LEFT -> oneOfOrNull('L' to DOWN, 'R' to UP) { scaffolding.hasPath(robot + it.second) }
        }
        direction = turn?.second
        if (direction != null) {

            var distance = 0
            while (scaffolding.hasPath(robot + direction)) {
                distance++
                robot += direction
            }
            result += turn!!.first to distance
        }
    }

    return result
}

private fun List<Pair<Char, Int>>.asInput(): String = this.joinToString(",") { "${it.first},${it.second}" }

// Naive but fortunately enough for the puzzle
fun findRoutine(scaffolding: Scaffolding): Pair<String, Triple<String, String, String>> {
    val path = findPath(scaffolding)
        .also { logWithTime("path: ${it.asInput()}") }
        .also { logWithTime("pathLen: ${it.size}") }

    val possibleFunctions: List<Movement> = (1..5)
        .flatMap { s -> (0 until path.size - s).map { path.subList(it, it + s) } }
        .distinct()
        .filter { it.asInput().length <= 20 }
        .reversed()

    val possibleSequences = permutationsWithRepetitions(4, 8)
        .map { p -> p.filter { it != 3 } }
        .map { p -> p.map { 'A' + it } }
        .map { p -> listOf('A') + p + listOf('C') }
        .toList()

    return possibleFunctions.asSequence()
        .map { a -> (possibleFunctions.asSequence() + sequenceOf(emptyList())).map { a to it } }
        .flatten()
        .map { (a, b) -> possibleFunctions.asSequence().map { Triple(a, b, it) } }
        .flatten()
        .filter { path.take(it.first.size) == it.first }
        .filter { path.subList(path.size - it.third.size, path.size) == it.third }
        .filter { it.first != it.second && it.second != it.third && it.third != it.first }
        .filter { path.take(it.first.size) == it.first }
        .flatMap { functions ->
            val (a, b, c) = functions
            possibleSequences.asSequence()
                .filter { p ->
                    p.map {
                        when (it) {
                            'A' -> a.size
                            'B' -> b.size
                            'C' -> c.size
                            else -> error("$it")
                        }
                    }.sum() == path.size
                }
                .filter { p ->
                    p.map {
                        when (it) {
                            'A' -> a
                            'B' -> b
                            'C' -> c
                            else -> error("$it")
                        }
                    }.flatten() == path
                }
                .map { p -> p to functions }
        }
        .map {
            it.first.joinToString(",") to it.second.let { t ->
                Triple(t.first.asInput(), t.second.asInput(), t.third.asInput())
            }
        }
        .single()

//    val mainRoutine = "A,B,A,B,A,C,B,C,A,C"
//    val functionA = "L,6,R,12,L,6"
//    val functionB = "R,12,L,10,L,4,L,6"
//    val functionC = "L,10,L,10,L,4,L,6"
//    return mainRoutine to Triple(functionA, functionB, functionC)
}


private val List<String>.innerIndices
    get() = (1..size - 2)
        .flatMap { y -> (1..this[y].length - 2).map { x -> y to x } }

private val List<String>.allIndices
    get() = indices
        .flatMap { y -> this[y].indices.map { x -> y to x } }

private fun alignment(scaffolding: Scaffolding): Int =
    scaffolding.map.innerIndices
        .filter { pos -> scaffolding.hasIntersection(pos) }
        .map { (row, column) -> row * column }
        .sum()

@FlowPreview
@ExperimentalStdlibApi
private fun drawMap(program: Memory): List<String> = runBlocking {
    val inChannel = Channel<BigInteger>()
    val outChannel = Channel<BigInteger>()
    val computer = Computer("ASCII", program, inChannel, outChannel)
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
    var lastData = ZERO!!
    return runBlocking {
        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>()
        val computer = Computer("ASCII", program, inChannel, outChannel)
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

@Suppress("BlockingMethodInNonBlockingContext")
private fun Flow<Char>.fullLines(): Flow<String> = flow {
    val builder = StringBuilder()
    collect {
        when (it) {
            '\n' -> emit(builder.toString()).also { builder.clear() }
            else -> builder.append(it)
        }
    }
}

suspend fun SendChannel<BigInteger>.writeln(msg: String) {
    println(msg)
    msg.map { it.toInt().toBigInteger() }.forEach { send(it) }
    send('\n'.toInt().toBigInteger())
}
