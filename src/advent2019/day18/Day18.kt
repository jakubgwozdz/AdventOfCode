package advent2019.day18

import advent2019.logWithTime
import advent2019.maze.Location
import advent2019.maze.Maze
import advent2019.maze.yx
import advent2019.pathfinder.BFSPathfinder
import advent2019.readAllLines
import kotlin.system.exitProcess


fun shortest(input: List<String>) = Vault(Maze(input)).shortest()

class Vault(val maze: Maze) {

    val mazeAsMap: Map<Location, Char> =
        maze.mapIndexed { y: Int, s: String -> s.mapIndexed { x: Int, c: Char -> (y yx x) to c } }
            .flatten()
            .toMap()
    val start = mazeAsMap.entries.single { (l, c) -> c == '@' }.key
    val keys = (mazeAsMap.filterValues { it.isLowerCase() }).map { (l, c) -> c to l }.toMap()
    val doors = mazeAsMap.filterValues { it.isUpperCase() }.map { (l, c) -> c to l }.toMap()

    val pois = keys + doors + ('@' to start)
    val allPaths = pois.map { s ->
        s.key to pois.mapNotNull { e ->
            when {
                s == e && e.key == '@' -> e.key to 0
                e.key == '@' -> null
                s == e -> null
                else -> {
                    val dist = maze.dist(s.value, e.value) { c -> c == '.' || c == '@' || c in keys }
                    if (dist != null) e.key to dist else null
                }
            }
        }.toMap()
    }.toMap()


    fun shortest(): Int {

        maze.input.forEach { logWithTime(it) }
        allPaths.forEach { logWithTime(it) }

        logWithTime("start: $start")
        logWithTime("keys: $keys")
        logWithTime("doors: $doors")

        val bfsPathfinder = BFSPathfinder<Path<Char>, List<Path<Char>>, Int>(
            logging = false,
            initialStateOp = { emptyList() },
            adderOp = { l, t ->
                l + t
//                val last = l.lastOrNull()?.e ?: maze[start]!!
//                l + Path(last, t, allPaths[last]!![t]!!)
            },
            distanceOp = this::distance,
            waysOutOp = this::waysOut
        )

        return bfsPathfinder.findShortest(Path('@', '@', 0)) { l, t -> (l.map { it.e } + t.e).containsAll(keys.keys) }!!
            .also { logWithTime(it) }
            .let { distance(it) }
    }

    private fun distance(pathsSoFar: List<Path<Char>>): Int {
        if (--test < 0) exitProcess(-1)
        val pathDist = pathsSoFar.sumBy { it.dist }
        logWithTime("${pathsSoFar.map { it.e }}: $pathDist")
        return pathDist
    }

    var test = 2500

    private fun waysOut(
        pathsSoFar: List<Path<Char>>,
        current: Path<Char>
    ): List<Path<Char>> {
        val visited = pathsSoFar.map { p -> p.e }
        val waysOut = allPaths[current.e]!!.keys
            .filter { it != current.e }
            .filter { it.isUpperCase() || it !in visited } // no need to go for key if it is already grabbed
            .filter { !it.isUpperCase() || it.toLowerCase() in visited }
            .filter {
                it !in visited || visited.lastIndexOf(it).let { lastIndex ->
                    visited.subList(lastIndex + 1, visited.size)
                        .any { k -> k.isLowerCase() && k !in visited.subList(0, lastIndex) }
                }
            }
            .map { Path(current.e, it, allPaths[current.e]!![it]!!) }
        return waysOut
    }
}

data class Path<T : Comparable<T>>(val s: T, val e: T, val dist: Int) : Comparable<Path<T>> {

    override fun compareTo(other: Path<T>): Int = compareValuesBy(this, other, { it.dist }, { it.s }, { it.e })

    override fun toString() = "$s->$e:$dist"

}

fun main() {
    val input = readAllLines("data/input-2019-18.txt")
    shortest(input)
        .also { logWithTime("part 1: $it") }
}
