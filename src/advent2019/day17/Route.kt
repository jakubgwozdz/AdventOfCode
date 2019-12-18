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

    return possibleA(path)
        .flatMap { a -> possibleC(path, a).map { c -> a to c } }
        .flatMap { (a, c) -> possibleB(path, a, c).map { b -> Triple(a, b, c) } }
        .filter { it.first != it.second && it.second != it.third && it.third != it.first }
        .filter { path.endsWith(it.third) || path.endsWith(it.second) || path.endsWith(it.first) }
        .onEach { logWithTime("${it.first.asInput()} | ${it.second.asInput()} | ${it.third.asInput()}") }
        .flatMap { functions -> possibleRoutines(path, functions).map { it to functions } }
        .map {
            it.first.joinToString(",") to it.second.let { t ->
                Triple(t.first.asInput(), t.second.asInput(), t.third.asInput())
            }
        }
        .first()
}

private fun possibleA(path: Movements): Sequence<Movements> {
    return (1..5) // functionA is from beginning, at least 1 item long
        .reversed()
        .asSequence()
        .map { path.take(it) as Movements }
        .filter { it.asInput().length <= 20 }
}

private fun possibleB(
    path: Movements,
    a: Movements,
    c: Movements
): Sequence<Movements> {
    val mustBeSecond = !path.startsWith(a, a.size) && !path.startsWith(c, a.size)
    val endsWithA = path.endsWith(a)
    val endsWithC = !endsWithA || path.endsWith(c)
    val mustBeSecondToLast = !(endsWithA && (path.endsWith(a, a.size) || path.endsWith(c, a.size)))
            && !(endsWithC && (path.endsWith(a, c.size) || path.endsWith(c, c.size)))
    val endLen = when {
        endsWithA && endsWithC -> a.size.coerceAtMost(c.size)
        endsWithC -> c.size
        else -> a.size
    }
    return when {
        mustBeSecond -> (5 downTo 1).map { path.subList(a.size, a.size + it) }
        mustBeSecondToLast -> (5 downTo 1).map { path.subList(path.size - endLen - it, path.size - endLen) }
        else -> (5 downTo 0)
            .flatMap { s -> (a.size until path.size - s).map { path.subList(it, it + s) } }
            .distinct()
    }
        .asSequence()
        .filter { it.asInput().length <= 20 }

}

private fun possibleC(path: Movements, a: Movements): Sequence<List<Movement>> =
    if (path.endsWith(a))
        (5 downTo 0).flatMap { s -> (0 until path.size - s).map { path.subList(it, it + s) } }.distinct()
            .asSequence()
            .filter { it.asInput().length <= 20 }
    else // if begin and end function is not the same, make C as end
        (5 downTo 1).map { path.subList(path.size - it, path.size) }
            .asSequence()
            .filter { it.asInput().length <= 20 }

fun possibleRoutines(
    path: Movements,
    functions: Triple<Movements, List<Movement>, List<Movement>>
): Sequence<List<Char>> {
    val movementsOp: (Char) -> Movements = {
        when (it) {
            'A' -> functions.first
            'B' -> functions.second
            'C' -> functions.third
            else -> error("$it")
        }
    }

    return permutationsWithRepetitions(4, 10)
        .map { p -> p.filter { it > 0 } } // skip 0 as "no call"
        .filter { it.size > 1 }
        .distinct()
        .map { p -> p.map { 'A' + it - 1 } }
        .filter { it[0] == 'A' } // routine always start with 'A'
        .filter { path.endsWith(movementsOp.invoke(it.last())) }
        .filter { p -> p.map(movementsOp).sumBy { it.size } == path.size }
        .filter {
            it.asSequence()
                .flatMap { c -> movementsOp.invoke(c).asSequence() }
                .beginsSameAs(path.asSequence())
        }
}

fun <T> Sequence<T>.beginsSameAs(o: Sequence<T>): Boolean {
    return zip(o).all { (a, b) -> a == b }
}

fun <T> List<T>.endsWith(o: List<T>, ending: Int = size): Boolean =
    size >= ending && ending >= o.size && subList(ending - o.size, ending) == o

fun <T> List<T>.startsWith(o: List<T>, starting: Int = 0): Boolean =
    size >= starting+o.size && subList(starting, starting + o.size) == o
