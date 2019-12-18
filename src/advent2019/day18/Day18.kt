package advent2019.day18

import advent2019.day15.plus
import advent2019.logWithTime
import advent2019.readAllLines
import java.math.BigInteger
import kotlin.system.exitProcess


typealias Location = Pair<Int, Int>
typealias Delta = Pair<Int, Int>

enum class Direction(val code: BigInteger, val delta: Delta) {
    N(1.toBigInteger(), -1 to 0),
    S(2.toBigInteger(), 1 to 0),
    W(3.toBigInteger(), 0 to -1),
    E(4.toBigInteger(), 0 to 1)
}

operator fun Location.plus(d: Direction) = this + d.delta
operator fun Location.plus(delta: Delta) = first + delta.first to second + delta.second
operator fun Location.minus(what: Location) = Direction.values().single { this == what + it }

fun List<Location>.from(pos: Location): List<Direction> {
    var acc = pos
    return map { n -> (n - acc).also { acc = n } }
}

typealias Maze = List<String>

operator fun Maze.get(what: Location) = this[what.first][what.second]

fun Maze.canGoThrough(where: Location, keys: Set<Char> = emptySet()) = where.first in 0 until size
        && where.second in this[where.first].indices
        && this[where].let { it != '#' && !(it.isUpperCase() && it.toLowerCase() !in keys) }

class Cache() {
    val c: MutableMap<Triple<Location, Location, Set<Char>>, Int> = mutableMapOf()
    val d: MutableMap<Pair<Location, Location>, Int> = mutableMapOf() // direct, no doors between

    fun compute(start: Location, end: Location, ownedKeys: Set<Char>, op: () -> Int?): Int? {
        if (start == end) return 0
        if (start > end) return compute(end, start, ownedKeys, op)
        return d[start to end]
            ?: Triple(start, end, ownedKeys).let { t ->
                return c[t] ?: op()?.also { c[t] = it }
            }
    }

}

private operator fun Location.compareTo(that: Location): Int {
    return this.first.compareTo(that.first)
        .let { if (it == 0) this.second.compareTo(that.second) else it }
}


fun moves(maze: Maze): Int {

    maze.forEach { logWithTime(it) }
    val mazeAsMap: Map<Location, Char> =
        maze.mapIndexed { y: Int, s: String -> s.mapIndexed { x: Int, c: Char -> (y to x) to c } }
            .flatten()
            .toMap()
    val pos = mazeAsMap.entries.single { (l, c) -> c == '@' }.key
    val keys = (mazeAsMap.filterValues { it.isLowerCase() || it == '@' }).map { (l, c) -> c to l }.toMap()
    val doors = mazeAsMap.filterValues { it.isUpperCase() }.map { (l, c) -> c to l }.toMap()

    logWithTime("start: $pos")
    logWithTime("keys: $keys")
    logWithTime("doors: $doors")

    val cache = Cache()
    keys.values.flatMap { a -> keys.values.map { b -> a to b } }
        .filter { (a, b) -> a < b }
        .mapNotNull { (a, b) -> shortestDistanceTo(a, b, maze, emptySet(), Cache())?.let { (a to b) to it } }
        .forEach { (pair, distance) -> cache.d[pair] = distance.also { logWithTime("$pair=$distance") } }

    return pathToAllKeys(pos, maze, keys, emptySet(), cache)
        .also { logWithTime("$it") }
        .let { it.sumBy { (_, l) -> l } }

}

var maxc = 1000
fun pathToAllKeys(
    start: Location,
    maze: Maze,
    keys: Map<Char, Location>,
    ownedKeys: Set<Char> = emptySet(),
    cache: Cache
): List<Pair<Char, Int>> {
    maxc--
    if (maxc ==0) exitProcess(-2)
    val possible = keys
        .filter { it.key != '@' }
        .filter { it.key !in ownedKeys }
        .mapNotNull { (k, l) ->
            shortestDistanceTo(start, l, maze, ownedKeys, cache)
                ?.let { k to it }
                .also { logWithTime("$k with shortest from $start to $l with ownedKeys=$ownedKeys) = ${it?.second}") }
        }.toMap()
    return possible
        .map { (k, len) -> listOf(k to len) + pathToAllKeys(keys[k]!!, maze, keys, ownedKeys + k, cache) }
        .minBy { it.sumBy { (k, l) -> l } }
        .orEmpty()

}


fun shortestPathTo(
    start: Location,
    end: Location,
    maze: List<String>,
    ownedKeys: Set<Char>,
    cache: MutableMap<Pair<Location, Location>, List<Location>>,
    visited: List<Location> = emptyList()
): List<Location>? {
    return Direction.values()
        .map { end + it }
        .filter { !visited.contains(it) }
        .filter { maze.canGoThrough(it, ownedKeys) }
        .mapNotNull { newEnd ->
            val c = start to newEnd
            cache[c]
                ?: shortestPathTo(start, newEnd, maze, ownedKeys, cache, visited + newEnd)
                    ?.also { cache[c] = it }
        }
        .minBy { it.size }
        ?.let { it + end }
}

fun shortestDistanceTo(
    start: Location,
    end: Location,
    maze: List<String>,
    ownedKeys: Set<Char>,
    cache: Cache,
    visited: List<Location> = emptyList()
): Int? {
    return Direction.values()
        .map { end + it }
        .filter { !visited.contains(it) }
        .filter { maze.canGoThrough(it, ownedKeys) }
        .mapNotNull { newEnd ->
            cache.compute(start, newEnd, ownedKeys) {
                shortestDistanceTo(start, newEnd, maze, ownedKeys, cache, visited + newEnd)
            }
                ?.let { it + 1 }
        }
        .min()
}


fun main() {
    val input = readAllLines("input-2019-18.txt")
    moves(input)
        .also { logWithTime("part 1: $it") }
}
