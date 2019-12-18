package advent2019.day17

import advent2019.day17.Direction.*
import advent2019.logWithTime
import advent2019.oneOfOrNull
import advent2019.permutationsWithRepetitions

enum class Direction(val char: Char, val change: Pair<Int, Int>) {
    UP('^', -1 to 0),
    DOWN('v', 1 to 0),
    LEFT('<', 0 to -1),
    RIGHT('>', 0 to 1)
}

typealias Position = Pair<Int, Int>

operator fun Position.plus(d: Direction) = first + d.change.first to second + d.change.second

typealias Movement = Pair<Char, Int>

typealias Movements = List<Movement>

private fun Movements.asInput(): String = this.joinToString(",") { "${it.first},${it.second}" }


class Scaffolding(val map: List<String>) {
    operator fun get(pos: Position): Char {
        val (y, x) = pos
        return if (y !in map.indices || x !in map[y].indices) '.'
        else map[y][x]
    }

    val innerIndices
        get() = (1..map.size - 2).flatMap { y ->
            (1..map[y].length - 2).map { x -> y to x }
        }

    fun hasIntersection(pos: Position): Boolean =
        hasPath(pos) && Direction.values().all { hasPath(pos + it) }

    fun hasPath(pos: Position) = this[pos] == '#'

    val robot
        get() = map.indices.flatMap { y ->
            map[y].indices.map { x -> y to x }
        }.single { (y, x) -> map[y][x] != '#' && map[y][x] != '.' }
}

// Naive but fortunately enough for the puzzle
fun findRoute(scaffolding: Scaffolding): Movements {
    var robot = scaffolding.robot
    var direction: Direction? = Direction.values().single { it.char == scaffolding[robot] }

    val result = mutableListOf<Movement>()
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

// Naive but fortunately enough for the puzzle
fun findRoutine(scaffolding: Scaffolding): Pair<String, Triple<String, String, String>> {
    val path = findRoute(scaffolding)
        .also { logWithTime("path: ${it.asInput()}") }
        .also { logWithTime("pathLen: ${it.size}") }

    val possibleFunctionA: List<Movements> = (1..5)
        .map { path.take(it) }
        .distinct()
        .filter { it.asInput().length <= 20 }
        .reversed()

    val possibleFunctionB: List<Movements> = (0..5)
        .flatMap { s -> (0 until path.size - s).map { path.subList(it, it + s) } }
        .reversed()
        .distinct()
        .filter { it.asInput().length <= 20 }

    val possibleFunctionC: List<Movements> = (0..5)
        .flatMap { s -> (0 until path.size - s).map { path.subList(it, it + s) } }
        .reversed()
        .distinct()
        .filter { it.asInput().length <= 20 }

    return possibleFunctionA.asSequence()
        .map { a -> (possibleFunctionB).map { a to it } }
        .flatten()
        .map { (a, b) -> possibleFunctionC.asSequence().map { Triple(a, b, it) } }
        .flatten()
        .filter { it.first != it.second && it.second != it.third && it.third != it.first }
        .filter { path.endsWith(it.third) || path.endsWith(it.second) || path.endsWith(it.first) }
        .onEach { logWithTime("${it.first.asInput()} | ${it.second.asInput()} | ${it.third.asInput()}") }
        .flatMap { functions ->

            val movementsOp: (Char) -> Movements = {
                when (it) {
                    'A' -> functions.first
                    'B' -> functions.second
                    'C' -> functions.third
                    else -> error("$it")
                }
            }

            permutationsWithRepetitions(4, 10)
                .map { p -> p.map { 'A' + it - 1 } }
                .map { p -> p.filter { it in 'A'..'C' } }
                .filter { it.size > 1 }
                .filter { it[0] == 'A' && (it[1] == 'A' || it[1] == 'B') }
                .filter { val l = movementsOp.invoke(it.last()) ; path.takeLast(l.size) == l }
                .filter { p -> p.map(movementsOp).sumBy { it.size } == path.size }
                .filter {
                    it.asSequence()
                        .flatMap { c -> movementsOp.invoke(c).asSequence() }
                        .sameAs(path.asSequence())
                }
//                .filter { p -> p.map(movementsOp).flatten() == path }
                .map { p -> p to functions }
        }
        .map {
            it.first.joinToString(",") to it.second.let { t ->
                Triple(t.first.asInput(), t.second.asInput(), t.third.asInput())
            }
        }
        .first()
}

fun <T> Sequence<T>.sameAs(o: Sequence<T>): Boolean {
    return zip(o).all { (a, b) -> a == b }
}

fun <T> List<T>.endsWith(o: List<T>): Boolean = takeLast(o.size) == o