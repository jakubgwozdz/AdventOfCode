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
    fun computeIfAbsent(start: T, end: T, accOp: () -> R, op: () -> R?): R?
}

open class BasicDistanceCache<T, R>(val logging: Boolean = false) : DistanceCache<T, R> {
    private val c: MutableMap<Pair<T, T>, R?> = mutableMapOf()

    override fun computeIfAbsent(start: T, end: T, accOp: () -> R, op: () -> R?): R? {
        val pair = start to end
        return when {
            start == end -> accOp()
            c.containsKey(pair) -> return c[pair].also { if (logging) logWithTime("previously found: $pair -> $it") }
            else -> op().also { c[pair] = it }.also { if (logging) logWithTime("adding to cache: $pair -> $it") }
        }
    }
}


class PathCache<T : Comparable<T>, D>(logging: Boolean) : BasicDistanceCache<T, List<D>>(logging) {

    override fun computeIfAbsent(start: T, end: T, accOp: () -> List<D>, op: () -> List<D>?): List<D>? {
        return when {
            start > end -> super.computeIfAbsent(end, start, accOp, op)?.reversed()
            else -> super.computeIfAbsent(start, end, accOp, op)
        }
    }
}

class NoCache<T : Comparable<T>, D>(logging: Boolean) : DistanceCache<T, List<D>> {

    override fun computeIfAbsent(start: T, end: T, accOp: () -> List<D>, op: () -> List<D>?): List<D>? {
        return if (start == end) accOp()
        else op()
    }
}

fun <T : Comparable<T>> shortestPath(
    start: T,
    end: T,
    logging: Boolean = false,
    waysOutOp: (T) -> Iterable<T>
): List<T>? {
    if (logging) logWithTime("looking for $start-$end")
    return shortest<T, List<T>>(
        start = start,
        end = end,
        logging = logging,
        cache = PathCache(logging),
        visited = emptyList(),
        accOp = { listOf(start) },
        adderOp = { l, e -> l + e },
        selector = { it.size },
        waysOutOp = waysOutOp
    )
}

fun <T : Comparable<T>, D : Any> shortest(
    start: T,
    end: T,
    logging: Boolean,
    cache: DistanceCache<T, D>,
    visited: List<T>,
    accOp: () -> D,
    adderOp: (D, T) -> D,
    selector: (D) -> Int,
    waysOutOp: (T) -> Iterable<T>
): D? {
    return waysOutOp(end)
        .asSequence()
        .filter { !visited.contains(it) }
        .mapNotNull { newEnd ->
            cache.computeIfAbsent(start, newEnd, accOp = accOp) {
                shortest(start, newEnd, logging, cache, visited + newEnd, accOp, adderOp, selector, waysOutOp)
            }
        }
        .map { adderOp(it, end) }
        .minBy(selector)
        ?.also { if (logging) logWithTime("found: $it") }
}
