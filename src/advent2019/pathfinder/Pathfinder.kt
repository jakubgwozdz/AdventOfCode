package advent2019.pathfinder

import advent2019.logWithTime

interface Pathfinder<T : Any, R : Any> {
    fun findShortest(start: T, end: T): R?
}

open class DFSPathfinder<T : Any, R : Any>(
    val logging: Boolean,
    val cache: Cache<T, R>,
    val initialStateOp: (T) -> R,
    val stopOp: (R, T) -> Boolean,
    val adderOp: (R, T) -> R,
    val selector: (R) -> Int,
    val waysOutOp: (R, T) -> Iterable<T>
) : Pathfinder<T, R> {

    override fun findShortest(start: T, end: T): R? {
        return findShortestProcess(start, end, initialStateOp(end))
    }

    private fun findShortestProcess(start: T, end: T, visited: R): R? = waysOutOp(visited, end)
        .asSequence()
        .filter { !stopOp(visited, it) }
        .mapNotNull { newEnd ->
            if (start == newEnd) initialStateOp(newEnd)
            else cache.computeIfAbsent(start, newEnd) { ns, ne -> findShortestProcess(ns, ne, adderOp(visited, ne)) }
        }
        .map { adderOp(it, end) }
        .minBy(selector)
        ?.also { if (logging) logWithTime("found: $it") }
}

class BasicDFSPathfinder<T : Any>(
    logging: Boolean = false,
    cache: Cache<T, List<T>> = NoCache(logging),
    initialStateOp: (T) -> List<T> = { t -> listOf(t) },
    stopOp: (List<T>, T) -> Boolean = { l, t -> t in l },
    adderOp: (List<T>, T) -> List<T> = { l, t -> l + t },
    selector: (List<T>) -> Int = { l -> l.size },
    waysOutOp: (List<T>, T) -> Iterable<T>
) : DFSPathfinder<T, List<T>>(logging, cache, initialStateOp, stopOp, adderOp, selector, waysOutOp)


interface Cache<T, R> {
    fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R?
}

open class BasicDistanceCache<T, R>(val logging: Boolean = false) :
    Cache<T, R> {
    private val c: MutableMap<Pair<T, T>, R?> = mutableMapOf()

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R? {
        val pair = start to end
        return when {
            start == end -> error("checking for $start<->$end")
            c.containsKey(pair) -> return c[pair].also { if (logging) logWithTime("already in cache: $pair -> $it") }
            else -> op(start, end).also {
                c[pair] = it
            }.also { if (logging) logWithTime("adding to cache: $pair -> $it") }
        }
    }
}

class PathCache<T : Comparable<T>, D>(logging: Boolean, val reverseOp: (D) -> D = { it }) :
    BasicDistanceCache<T, List<D>>(logging) {

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = when {
        start > end -> super.computeIfAbsent(end, start, op)?.reversed()?.map(reverseOp)
        else -> super.computeIfAbsent(start, end, op)
    }
}

class NoCache<T, D>(
    logging: Boolean
) : Cache<T, List<D>> {
    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = when {
        start == end -> error("checking for $start<->$end")
        else -> op(start, end)
    }
}

fun <T : Comparable<T>> shortestPath(
    start: T,
    end: T,
    logging: Boolean = false,
    waysOutOp: (T, List<T>) -> Iterable<T>
): List<T>? {
    if (logging) logWithTime("looking for $start-$end")
    return shortest<T, List<T>>(
        start = start,
        end = end,
        logging = logging,
        cache = PathCache(logging),
        visited = emptyList(),
        initOp = { listOf(it) },
        adderOp = { l, e -> l + e },
        selector = { it.size },
        waysOutOp = waysOutOp
    )
}

fun <T, D : Any> shortest(
    start: T,
    end: T,
    logging: Boolean,
    cache: Cache<T, D>,
    visited: List<T>,
    initOp: (T) -> D,
    stopOp: (List<T>, T) -> Boolean = { v, t -> v.contains(t) },
    adderOp: (D, T) -> D,
    selector: (D) -> Int,
    waysOutOp: (T, List<T>) -> Iterable<T>
): D? {
    return waysOutOp(end, visited)
        .asSequence()
        .filter { !stopOp(visited, it) }
        .mapNotNull { newEnd ->
            if (start == newEnd) initOp(start) else
                cache.computeIfAbsent(start, newEnd) { ns, ne ->
                    shortest(
                        ns,
                        ne,
                        logging,
                        cache,
                        visited + ne,
                        initOp,
                        stopOp,
                        adderOp,
                        selector,
                        waysOutOp
                    )
                }
        }
        .map { adderOp(it, end) }
        .minBy(selector)
        ?.also { if (logging) logWithTime("found: $it") }
}