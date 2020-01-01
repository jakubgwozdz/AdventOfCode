package advent2019.day18

import advent2019.logWithTime
import advent2019.maze.Location
import advent2019.maze.Maze
import advent2019.maze.yx
import advent2019.pathfinder.BFSPathfinder
import advent2019.readAllLines


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
            if (s == e) e.key to 0
            else maze.dist(s.value, e.value)?.let { e.key to it }
        }.toMap()
    }.toMap()


    fun shortest(): Int {

        maze.input.forEach { logWithTime(it) }

        logWithTime("start: $start")
        logWithTime("keys: $keys")
        logWithTime("doors: $doors")

        val bfsPathfinder = BFSPathfinder<Char, List<Path<Char>>, Int>(
            logging = false,
            initialStateOp = { emptyList() },
            adderOp = { l, t ->
                val last = l.lastOrNull()?.e ?: maze[start]!!
                l + Path(last, t, allPaths[last]!![t]!!)
            },
            distanceOp = { l -> distance(l) },
            waysOutOp = this::waysOut
        )

        return bfsPathfinder.findShortest('@') { l, t -> (l.map { it.e } + t).containsAll(keys.keys) }!!
            .also { logWithTime(it) }
            .let { distance(it) / 100 }
    }

    private fun distance(pathsSoFar: List<Path<Char>>): Int {
        val pathDist = pathsSoFar.sumBy { it.dist }
        val keysToGet = (keys.keys - pathsSoFar.map { it.e })
        val result = pathDist * 100// + keysToGet.size
        logWithTime("$pathsSoFar: $pathDist*100 + $keysToGet.size = $result")
        return result
    }

    private fun waysOut(
        pathsSoFar: List<Path<Char>>,
        current: Char
    ): List<Char> {
        val visited = pathsSoFar.map { p -> p.e }
        val waysOut = allPaths[current]!!.keys
            .filter { it != current }
            .filter { !it.isUpperCase() || it.toLowerCase() in visited }
            .filter { it !in visited || visited.lastIndexOf(it).let { lastIndex ->
                visited.subList(lastIndex+1,visited.size).any { k->k.isLowerCase() && k !in visited.subList(0, lastIndex) }
            } }
        return waysOut
    }
}

data class Path<T : Comparable<T>>(val s: T, val e: T, val dist: Int) : Comparable<Path<T>> {

    override fun compareTo(other: Path<T>): Int = compareValuesBy(this, other, { it.dist }, { it.s }, { it.e })

    override fun toString() = "->$e"

}

fun main() {
    val input = readAllLines("data/input-2019-18.txt")
    shortest(input)
        .also { logWithTime("part 1: $it") }
}
