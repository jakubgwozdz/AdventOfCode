package advent2019.pathfinder

import advent2019.logWithTime

interface Pathfinder<T : Any, R : Any> {
    fun findShortest(start: T, end: T): R?
}

open class DFSPathfinder<T : Any, R : Any>(
    val logging: Boolean,
    val cache: Cache<T, R>,
    val initialStateOp: () -> R,
    val stopOp: (R, T) -> Boolean,
    val adderOp: (R, T) -> R,
    val distanceOp: ((R) -> Int)?,
    val comparator: Comparator<R> = distanceOp?.let { compareBy(it) } ?: error("Requires distanceOp or comparator"),
    val waysOutOp: (R, T) -> Iterable<T>
) : Pathfinder<T, R> {

    override fun findShortest(start: T, end: T): R? {
        return findShortestProcess(start, end, initialStateOp())
    }

    private fun findShortestProcess(from: T, end: T, visited: R): R? {
        val newVisited = adderOp(visited, from)
        if (from == end) return newVisited
        return waysOutOp(newVisited, from)
            .also { if (logging) logWithTime("WaysOut for $from: $it") }
            .asSequence()
            .mapNotNull { next ->
//                if (next == end) adderOp(visited, next)
//                else
                    cache.computeIfAbsent(next, end) { ns, ne -> findShortestProcess(ns, ne, newVisited) }
            }
            .run { if (distanceOp != null) minBy(distanceOp) else minWith(comparator) }
            ?.also { if (logging) logWithTime("found: $it") }
    }
}

open class BFSPathfinder<T : Any, R : Any, I : Comparable<I>>(
    val logging: Boolean,
    val initialStateOp: () -> R,
    val stopOp: (R, T) -> Boolean,
    val adderOp: (R, T) -> R,
    val distanceOp: ((R) -> I),
//    val comparator: Comparator<R> = distanceOp?.let { compareBy(it) } ?: error("Requires distanceOp or comparator"),
    val waysOutOp: (R, T) -> Iterable<T>
) : Pathfinder<T, R> {

    override fun findShortest(start: T, end: T): R? {
        add(start, initialStateOp())
        while (toVisit.isNotEmpty()) {
            val next = pick()
            waysOutOp(next.second, next.first).forEach {
                //                if (stopOp(
            }

        }
        return currentBest?.first
    }

    private fun add(elem: T, prevState: R) {
        val nextState = adderOp(prevState, elem)
        val distance = distanceOp(nextState)
        if (currentBest == null || currentBest.second > distance) {
            toVisit.add(
                Triple(elem, nextState, distance)
            ).also { if (logging) logWithTime("adding $nextState with distance $distance") }
        } else if (logging) logWithTime("skipping $nextState with distance $distance, we got better result already")
    }

    private fun pick(): Pair<T, R> {
        val closest = toVisit.first()
        toVisit.remove(closest)
        return closest.first to closest.second
    }

    private val currentBest: Pair<R, I>? = null
    private val toVisit: MutableSet<Triple<T, R, I>> = sortedSetOf(compareBy { it.third })

}

class BasicPathfinder<T : Any>(
    logging: Boolean = false,
    cache: Cache<T, List<T>> = NoCache(),
    initialStateOp: () -> List<T> = { emptyList() },
    adderOp: (List<T>, T) -> List<T> = { l, t -> l + t },
    distanceOp: ((List<T>) -> Int) = { l -> l.size },
//    comparator: Comparator<List<T>> = distanceOp?.let { compareBy(it) } ?: error("Requires distanceOp or comparator"),
    waysOutOp: (List<T>, T) -> Iterable<T>
) : DFSPathfinder<T, List<T>>(
    logging,
    cache,
    initialStateOp,
    stopOp = { l, t -> t in l },
    adderOp = adderOp,
    distanceOp = distanceOp,
    waysOutOp = { l, t -> waysOutOp(l, t).filter { it !in l } }
)


interface Cache<T, R> {
    fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R?
}

open class BasicDistanceCache<T, R>(private val logging: Boolean = false) :
    Cache<T, R> {
    private val c: MutableMap<Pair<T, T>, R?> = mutableMapOf()

    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> R?): R? {
        val pair = start to end
        return when {
//            start == end -> error("checking for $start<->$end")
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
//        end -> error("checking for $start<->$end")
        else -> op(start, end)
    }
}
