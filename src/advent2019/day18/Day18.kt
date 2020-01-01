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

        val cache = keys.mapValues { mutableListOf<Pair<List<Char>, Int>>() }

        val bfsPathfinder = BFSPathfinder<Path<Char>, SearchState<Char>, Int>(
            loggingFound = true,
            initialStateOp = { SearchState(emptyList()) },
            adderOp = { l, t -> l + t },
            distanceOp = this::distance,
            meaningfulOp = { l, d -> worthChecking(l, d, cache) },
            priority = (compareByDescending { it.second.list.size }),
            waysOutOp = this::waysOut
        )

        return bfsPathfinder.findShortest(Path('@', '@', 0), this::found)!!
            .also { logWithTime(it) }
            .let { distance(it) }
    }

    private fun found(pathsSoFar: SearchState<Char>, next: Path<Char>): Boolean {
        val all = (pathsSoFar.ownedKeys + next.e).containsAll(keys.keys)
//        if (all) {
//            val stops = pathsSoFar.map { it.e }
//            println("$stops: ${distance(pathsSoFar)}")
//        }
        return all
    }

    private fun worthChecking(
        pathsSoFar: SearchState<Char>,
        distance: Int,
        cache: Map<Char, MutableList<Pair<List<Char>, Int>>>
    ): Boolean {
        val stops = pathsSoFar.stops
        val last = stops.last()
        if (last == '@') return true
        val ownedKeys = stops.sorted().distinct()
        val checkedPathsHere = cache[last] ?: error("unknown key '$last'")
        val hasBetterCandidate = checkedPathsHere.any { (keys, d) ->
            keys.containsAll(ownedKeys) && d <= distance
        }
        return when {
            hasBetterCandidate -> {
//                logWithTime("has better candidate for $visited")
                false
            }
            else -> {
                checkedPathsHere.add(ownedKeys to distance)
                true
            }
        }

    }

    private fun distance(pathsSoFar: SearchState<Char>): Int {
//        if (--test < 0) exitProcess(-1)
        val distance = pathsSoFar.distance
//        val ownedKeys = pathsSoFar.map { it.e }.sorted().distinct()
//        val keysToGrab = keys - ownedKeys
//        val stops = pathsSoFar.map { it.e }.toString()
//        if (keysToGrab.size<1)
//            println("$stops: $distance, keysToGrab:$keysToGrab")

//        if ("[@, a, f, b, j, g, n, h, d, l, o, e, p, c, i, k, m".startsWith(s.dropLast(1)))
//        println("$s: $pathDist")

        return distance //+ keysToGrab.size * 1000
    }

    var test = 2500

    private fun waysOut(
        pathsSoFar: SearchState<Char>,
        current: Path<Char>
    ): List<Path<Char>> {
        val visited = pathsSoFar.ownedKeys.intersect(keys.keys)
        val waysOut = keys
            .filter { it.key !in visited }
            .map {
                it.key to maze.dist(
                    pois[current.e]!!,
                    it.value
                ) { c -> c == '.' || c == '@' || (c != null && c.toLowerCase() in visited) }
            }
            .mapNotNull { (e, d) -> if (d != null) Path(current.e, e, d) else null }
        return waysOut
    }
}

data class SearchState<T : Comparable<T>>(val list: List<Path<T>>) {
    operator fun plus(t: Path<T>): SearchState<T> {
        return SearchState(list + t)
    }

    val stops = list.map { p -> p.e }

    val ownedKeys = stops.toSet()

    val distance = list.sumBy { it.dist }
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
