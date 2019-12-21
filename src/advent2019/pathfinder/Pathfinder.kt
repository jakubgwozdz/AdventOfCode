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
    val comparator: Comparator<R>,
    val waysOutOp: (R, T) -> Iterable<T>
) : Pathfinder<T, R> {

    override fun findShortest(start: T, end: T): R? {
        return findShortestProcess(start, end, initialStateOp(start))
    }

    private fun findShortestProcess(start: T, end: T, visited: R): R? = waysOutOp(visited, start)
        .asSequence()
        .filter { !stopOp(visited, it) }
        .mapNotNull { newStart ->
            if (newStart == end) initialStateOp(newStart)
            else cache.computeIfAbsent(newStart, end) { ns, ne -> findShortestProcess(ns, ne, adderOp(visited, ns)) }
        }
        .map { adderOp(it, start) }
        .minWith(comparator)
        ?.also { if (logging) logWithTime("found: $it") }
}

class BasicPathfinder<T : Any>(
    logging: Boolean = false,
    cache: Cache<T, List<T>> = NoCache(),
    initialStateOp: (T) -> List<T> = { t -> listOf(t) },
    stopOp: (List<T>, T) -> Boolean = { l, t -> t in l },
    adderOp: (List<T>, T) -> List<T> = { l, t -> listOf(t)+l },
    comparator: Comparator<List<T>> = compareBy { it.size },
    waysOutOp: (List<T>, T) -> Iterable<T>
) : DFSPathfinder<T, List<T>>(logging, cache, initialStateOp, stopOp, adderOp, comparator, waysOutOp)


interface Cache<T, R> {
    fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R?
}

open class BasicDistanceCache<T, R>(private val logging: Boolean = false) :
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

class PathCache<T : Comparable<T>, D>(logging: Boolean, private val reverseOp: (D) -> D = { it }) :
    BasicDistanceCache<T, List<D>>(logging) {

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = when {
        start > end -> super.computeIfAbsent(end, start, op)?.reversed()?.map(reverseOp)
        else -> super.computeIfAbsent(start, end, op)
    }
}

class NoCache<T, D> : Cache<T, List<D>> {
    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = when (start) {
        end -> error("checking for $start<->$end")
        else -> op(start, end)
    }
}
