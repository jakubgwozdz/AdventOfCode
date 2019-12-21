package advent2019.maze

import advent2019.logWithTime
import java.math.BigInteger

data class Location(val y: Int, val x: Int) : Comparable<Location> {
    override fun toString() = "$y:$x"
    override fun compareTo(other: Location) = comparator.compare(this, other)

    companion object {
        val comparator = compareBy<Location>({ it.y }, { it.x })
    }
}

infix fun Int.yx(x: Int) = Location(this, x)

typealias Delta = Pair<Int, Int>

enum class Direction(val code: BigInteger, val delta: Delta) {
    N(1.toBigInteger(), -1 to 0),
    S(2.toBigInteger(), 1 to 0),
    W(3.toBigInteger(), 0 to -1),
    E(4.toBigInteger(), 0 to 1)
}

operator fun Location.plus(d: Direction) = this + d.delta
operator fun Location.plus(delta: Delta) = y + delta.first yx x + delta.second
operator fun Location.minus(what: Location) = Direction.values().single { this == what + it }

fun List<Location>.from(pos: Location): List<Direction> {
    var acc = pos
    return map { n -> (n - acc).also { acc = n } }
}

typealias Maze = List<String>

operator fun Maze.get(what: Location) =
    if (what.y in this.indices && what.x in this[what.y].indices) this[what.y][what.x] else null

interface DistanceCache<T, R> {
    fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R?
}

open class BasicDistanceCache<T, R>(val logging: Boolean = false, val initOp: (T, T) -> R) : DistanceCache<T, R> {
    private val c: MutableMap<Pair<T, T>, R?> = mutableMapOf()

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R? {
        val pair = start to end
        return when {
            start == end -> initOp(start, end)
            c.containsKey(pair) -> return c[pair].also { if (logging) logWithTime("previously found: $pair -> $it") }
            else -> op(start, end).also {
                c[pair] = it
            }.also { if (logging) logWithTime("adding to cache: $pair -> $it") }
        }
    }
}


class PathCache<T : Comparable<T>, D>(logging: Boolean, initOp: (T, T) -> List<D>) :
    BasicDistanceCache<T, List<D>>(logging, initOp) {

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? {
        return when {
            start > end -> super.computeIfAbsent(end, start, op)?.reversed()
            else -> super.computeIfAbsent(start, end, op)
        }
    }
}

class NoCache<T, D>(
    logging: Boolean, val stopCondition: (T, T) -> Boolean = { a, b -> a == b },
    val initOp: (T, T) -> List<D>
) : DistanceCache<T, List<D>> {

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? {
        return if (stopCondition(start, end)) initOp(start, end)
        else op(start, end)
    }
}

fun <T : Comparable<T>> shortestPath(
    start: T,
    end: T,
    logging: Boolean = false,
    waysOutOp: (T,List<T>) -> Iterable<T>
): List<T>? {
    if (logging) logWithTime("looking for $start-$end")
    return shortest<T, List<T>>(
        start = start,
        end = end,
        logging = logging,
        cache = PathCache(logging) { a, b -> listOf(a) },
        visited = emptyList(),
        adderOp = { l, e -> l + e },
        selector = { it.size },
        waysOutOp = waysOutOp
    )
}

fun <T, D : Any> shortest(
    start: T,
    end: T,
    logging: Boolean,
    cache: DistanceCache<T, D>,
    visited: List<T>,
    stopOp: (List<T>, T) -> Boolean = { v, t -> v.contains(t) },
    adderOp: (D, T) -> D,
    selector: (D) -> Int,
    waysOutOp: (T, List<T>) -> Iterable<T>
): D? {
    return waysOutOp(end, visited)
        .asSequence()
        .filter { !stopOp(visited, it) }
        .mapNotNull { newEnd ->
            cache.computeIfAbsent(start, newEnd) { ns, ne ->
                shortest(ns, ne, logging, cache, visited + ne, stopOp, adderOp, selector, waysOutOp)
            }
        }
        .map { adderOp(it, end) }
        .minBy(selector)
        ?.also { if (logging) logWithTime("found: $it") }
}
