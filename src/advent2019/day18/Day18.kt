package advent2019.day18

import advent2019.logWithTime
import advent2019.maze.Location
import advent2019.maze.Maze
import advent2019.maze.yx
import advent2019.pathfinder.BFSPathfinder
import advent2019.pathfinder.BasicPathfinder
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
                    val dist = maze.dist(s.value, e.value) { c -> c == '.' || c == '@' /*|| c in keys*/ }
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

        val cache = keys.mapValues { mutableListOf<Pair<Set<Char>, Int>>() }

        return BFSPathfinder(
//            logging = true,
            loggingFound = true,
            initialStateOp = { SearchState(emptyList()) },
            adderOp = { l, t -> l + t },
            distanceOp = SearchState<Char>::distance,
            meaningfulOp = { l, d -> worthChecking(l, d, cache) },
            priority = compareBy { it.second.distance },
            waysOutOp = this::waysOut
        ).findShortest(Segment('@', '@', 0), this::found)!!
            .also { logWithTime(it) }
            .distance
    }

    private fun found(pathsSoFar: SearchState<Char>, next: Segment<Char>): Boolean =
        (pathsSoFar.ownedKeys + next.e).containsAll(keys.keys)

    private fun worthChecking(
        pathsSoFar: SearchState<Char>,
        distance: Int,
        cache: Map<Char, MutableList<Pair<Set<Char>, Int>>>
    ): Boolean {
        val stops = pathsSoFar.stops
        if (stops.isEmpty()) return true
        val last = stops.last()
        val ownedKeys = pathsSoFar.ownedKeys
        val checkedPathsHere = cache[last] ?: error("unknown key '$last'")
        val hasBetterCandidate = checkedPathsHere.any { (keys, d) ->
            keys.containsAll(ownedKeys) && d <= distance
        }
        return when {
            hasBetterCandidate -> {
                false
            }
            else -> {
                checkedPathsHere.add(ownedKeys to distance)
                true
            }
        }

    }

    var test = 2500

    private fun waysOut(
        pathsSoFar: SearchState<Char>,
        current: Segment<Char>
    ): List<Segment<Char>> {
        val ownedKeys = pathsSoFar.ownedKeys
        return keys.keys
            .filter { it !in ownedKeys }
            .mapNotNull { distanceBetweenPoints(current.e, it, ownedKeys) }
    }

    inner class CalculatedSegments {
        val cache: MutableMap<Triple<Char, Char, Set<Char>>, Int> = mutableMapOf()

        fun compute(s: Char, e: Char, ownedKeys: Set<Char>): Segment<Char>? = when {
            s == e -> Segment(s, e, 0)
            s > e -> compute(e, s, ownedKeys)?.let { Segment(it.e, it.s, it.dist) }
            else -> cache.computeIfAbsent(Triple(s, e, ownedKeys)) {
                BasicPathfinder<Char>(distanceOp = this@Vault::directDistance) { _, t ->
                    allPaths[t]!!.keys
                        .filter { t1 -> t1 == e || t1.toLowerCase() in ownedKeys }
                }
                    .findShortest(s, e)
                    ?.let(this@Vault::directDistance)
                    ?: -1
            }
                .let { if (it >= 0) Segment(s, e, it) else null }
        }
    }

    val calculatedSegments = CalculatedSegments()

    private fun distanceBetweenPoints(
        prevStep: Char,
        nextStep: Char,
        ownedKeys: Set<Char>
    ) = calculatedSegments.compute(prevStep, nextStep, ownedKeys)

    private val distanceCache = mutableMapOf<List<Char>, Int>()

    private fun directDistance(stops: List<Char>): Int {
        return distanceCache.computeIfAbsent(stops) {
            var s = stops.first()
            stops.fold(0) { a, c -> if (s == c) a else a + allPaths[s]!![c]!!.also { s = c } }
        }
    }
}

data class SearchState<T : Comparable<T>>(val list: List<Segment<T>>) {

    operator fun plus(t: Segment<T>): SearchState<T> = SearchState(list + t)

    override fun toString(): String = "$stops"

    val stops = list.drop(1).map { p -> p.e }

    val ownedKeys = stops.toSortedSet()

    val distance = list.sumBy { it.dist }
}


data class Segment<T : Comparable<T>>(val s: T, val e: T, val dist: Int) : Comparable<Segment<T>> {

    override fun compareTo(other: Segment<T>): Int = compareValuesBy(this, other, { it.dist }, { it.s }, { it.e })

    override fun toString() = "$s->$e:$dist"

}

fun main() {
    val input = readAllLines("data/input-2019-18.txt")
    shortest(input)
        .also { logWithTime("part 1: $it") }
}
