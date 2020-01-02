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

        return BFSPathfinder<Segment, SearchState, Int>(
//            logging = true,
            loggingFound = true,
            adderOp = { l, t -> l + t },
            distanceOp = SearchState::distance,
            meaningfulOp = { l, d -> worthChecking(l, d, cache) },
            priority = compareBy { it.first.distance },
            waysOutOp = this::waysOut
        )
            .findShortest(
                SearchState(start.mapValues { (r, _) -> listOf(Segment(r, r, r, 0)) })
            ) { pathsSoFar -> (pathsSoFar.ownedKeys).containsAll(this.keys.keys) }!!
            .also { logWithTime(it) }
            .distance
    }

    private fun worthChecking(
        pathsSoFar: SearchState,
        distance: Int,
        cache: Map<Char, MutableList<Pair<Set<Char>, Int>>>
    ): Boolean {
        val stops = pathsSoFar.lastMove
        if (pathsSoFar.paths.any { it.value.size < 2 }) return true
        val last = stops.single().e
        val ownedKeys = pathsSoFar.ownedKeys
        val checkedPathsHere = cache[last] ?: error("unknown key '$last'")
        val betterCandidate = checkedPathsHere.firstOrNull { (keys, d) ->
            keys.size >= ownedKeys.size && keys.containsAll(ownedKeys) && d <= distance
        }
        return when {
            betterCandidate != null -> {
//                logWithTime("$pathsSoFar not worth checking because of $betterCandidate")
                false
            }
            else -> {
                checkedPathsHere.add(ownedKeys to distance)
                true
            }
        }

    }

    private fun waysOut(pathsSoFar: SearchState): List<Segment> {
        val ownedKeys = pathsSoFar.ownedKeys
        val keysOfInterest = keys.keys
            .filter { it !in ownedKeys }
        return start.keys.flatMap { robot ->
            keysOfInterest
                .mapNotNull { distanceBetweenPoints(robot, pathsSoFar.paths[robot]!!.last().e, it, ownedKeys) }
        }
    }

    data class CacheKey(val r: Char, val s: Char, val e: Char, val ownedKeys: Set<Char>)

    inner class CalculatedSegments {

        val cache: MutableMap<CacheKey, Int> = mutableMapOf()

        fun compute(r: Char, s: Char, e: Char, ownedKeys: Set<Char>): Segment? = when {
            s == e -> Segment(r, s, e, 0)
            s > e -> compute(r, e, s, ownedKeys)?.let { Segment(it.robot, it.e, it.s, it.dist) }
            else -> cache.computeIfAbsent(CacheKey(r, s, e, ownedKeys)) { k ->
                BasicPathfinder<Char>(distanceOp = this@Vault::directDistance) { l ->
                    allPaths[l.last()]!!.keys
                        .filter { it == k.e || it.toLowerCase() in ownedKeys }
                }
                    .findShortest(listOf(k.s)) { it.lastOrNull() == k.e }
                    ?.let(this@Vault::directDistance)
                    ?: -1
            }
                .let { if (it >= 0) Segment(r, s, e, it) else null }
        }
    }

    val calculatedSegments = CalculatedSegments()

    private fun distanceBetweenPoints(
        robot: Char,
        prevStep: Char,
        nextStep: Char,
        ownedKeys: Set<Char>
    ) = calculatedSegments.compute(robot, prevStep, nextStep, ownedKeys)

    private val distanceCache = mutableMapOf<List<Char>, Int>()

    private fun directDistance(stops: List<Char>): Int {
        return distanceCache.computeIfAbsent(stops) {
            var s = stops.first()
            stops.fold(0) { a, c -> if (s == c) a else a + allPaths[s]!![c]!!.also { s = c } }
        }
    }
}

data class SearchState(val paths: Map<Char, List<Segment>>, val lastMove: List<Segment>) {
    constructor(paths: Map<Char, List<Segment>>) : this(paths, emptyList())

    operator fun plus(t: Segment): SearchState =
        SearchState(paths.mapValues { (robot, l) -> if (robot == t.robot) l + t else l }, listOf(t))

    override fun toString(): String = "$stops"

    val stops = paths.mapValues { it.value.drop(1).map { p -> p.e } }

    val ownedKeys = stops.values.flatten().toSortedSet()

    val distance = paths.values.map { l -> l.sumBy { it.dist } }.sum()
}


data class Segment(val robot: Char, val s: Char, val e: Char, val dist: Int) : Comparable<Segment> {

    override fun compareTo(other: Segment): Int =
        compareValuesBy(this, other, { it.robot }, { it.dist }, { it.s }, { it.e })

    override fun toString() = "$robot:$s->$e:$dist"

}

fun main() {
    val input = readAllLines("data/input-2019-18.txt")
    shortest(input)
        .also { logWithTime("part 1: $it") }
    shortest(split(input))
        .also { logWithTime("part 2: $it") }
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
