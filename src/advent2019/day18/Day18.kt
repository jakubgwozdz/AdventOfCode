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
    val start = mazeAsMap.filterValues { it == '@' }.map { (l, c) -> c to l }
        .fold(emptyList<Pair<Char, Location>>()) { a, i ->
            a + ((a.lastOrNull()?.let { it.first + 1 } ?: '0') to i.second)
        }
        .toMap()
    val keys = mazeAsMap.filterValues { it.isLowerCase() }.map { (l, c) -> c to l }.toMap()
    val doors = mazeAsMap.filterValues { it.isUpperCase() }.map { (l, c) -> c to l }.toMap()

    val pois = keys + doors + start
    val allPaths = pois.map { s ->
        s.key to pois.mapNotNull { e ->
            when {
                s == e && e.key.isDigit() -> e.key to 0
                e.key.isDigit() -> null
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

        val start1 = Segment('0', '0', 0)
        return BFSPathfinder<Segment<Char>, SearchState<Char>, Int>(
//            logging = true,
            loggingFound = true,
            adderOp = { l, t -> l + t },
            distanceOp = SearchState<Char>::distance,
            meaningfulOp = { l, d -> worthChecking(l, d, cache) },
            priority = compareBy { it.first.distance },
            waysOutOp = this::waysOut
        )
            .findShortest(SearchState<Char>(emptyList())+start1) { pathsSoFar ->
                (pathsSoFar.ownedKeys).containsAll(this.keys.keys)
            }!!
            .also { logWithTime(it) }
            .distance
    }

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
            keys.size >= ownedKeys.size && keys.containsAll(ownedKeys) && d <= distance
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
        pathsSoFar: SearchState<Char>
    ): List<Segment<Char>> {
        val ownedKeys = pathsSoFar.ownedKeys
        return keys.keys
            .filter { it !in ownedKeys }
            .mapNotNull { distanceBetweenPoints(pathsSoFar.list.last().e, it, ownedKeys) }
    }

    inner class CalculatedSegments {
        val cache: MutableMap<Triple<Char, Char, Set<Char>>, Int> = mutableMapOf()

        fun compute(s: Char, e: Char, ownedKeys: Set<Char>): Segment<Char>? = when {
            s == e -> Segment(s, e, 0)
            s > e -> compute(e, s, ownedKeys)?.let { Segment(it.e, it.s, it.dist) }
            else -> cache.computeIfAbsent(Triple(s, e, ownedKeys)) {
                BasicPathfinder<Char>(distanceOp = this@Vault::directDistance) { l ->
                    allPaths[l.last()]!!.keys
                        .filter { t1 -> t1 == e || t1.toLowerCase() in ownedKeys }
                }
                    .findShortest(listOf(s)) { l -> l.lastOrNull() == e }
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

fun split(input: List<String>): List<String> {
    val start = input.mapIndexed { y: Int, s: String ->
        s.mapIndexed { x: Int, c: Char -> (y yx x) to c }
    }
        .flatten()
        .toMap()
        .filterValues { it == '@' }
        .keys
        .single()

    return input.subList(0, start.y - 1) +
            (input[start.y - 1].substring(0, start.x - 1) + "@#@" + input[start.y - 1].substring(start.x + 2)) +
            (input[start.y + 0].substring(0, start.x - 1) + "###" + input[start.y + 0].substring(start.x + 2)) +
            (input[start.y + 1].substring(0, start.x - 1) + "@#@" + input[start.y + 1].substring(start.x + 2)) +
            input.subList(start.y + 2, input.size)
}
