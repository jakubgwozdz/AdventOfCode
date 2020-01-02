package advent2019.pathfinder

import advent2019.logWithTime
import java.util.*

/**
 * T - location
 * D - exits
 * R - state
 */
interface Pathfinder<T : Any, R : Any> {
    fun findShortest(startState: R, end: T): R?
}

open class DFSPathfinder<T : Any, D : Any, R : Any>(
    val logging: Boolean,
    val cache: Cache<T, R>,
    val initialStateOp: () -> R,
    val adderOp: (R, T) -> R,
    val distanceOp: ((R) -> Int)?,
    val comparator: Comparator<R> = distanceOp?.let { compareBy(it) } ?: error("Requires distanceOp or comparator"),
    val nextRoom: (R, D) -> T,
    val waysOutOp: (R, T) -> Iterable<D>
)  {

    fun findShortest(start: T, end: T): R? {
        return findShortestProcess(start, end, initialStateOp())
    }

    private fun findShortestProcess(from: T, end: T, visited: R): R? {
        val newVisited = adderOp(visited, from)
        if (from == end) return newVisited
        return waysOutOp(newVisited, from)
            .also { if (logging) logWithTime("WaysOut for $from: $it") }
            .asSequence()
            .map { nextRoom(newVisited, it) }
            .mapNotNull { next ->
                cache.computeIfAbsent(next, end) { ns, ne -> findShortestProcess(ns, ne, newVisited) }
            }
            .run { if (distanceOp != null) minBy(distanceOp) else minWith(comparator) }
            ?.also { if (logging) logWithTime("found: $it") }
    }
}

open class BFSPathfinder<T : Any, R : Any, I : Comparable<I>>(
    val logging: Boolean = false,
    val loggingFound: Boolean = false,
    val adderOp: (R, T) -> R,
    val distanceOp: ((R) -> I),
    val meaningfulOp: (R, I) -> Boolean = { _, _ -> true },
    val priority: Comparator<Pair<R, I>> = compareBy { it.second },
    val waysOutOp: (R) -> Iterable<T>
//) {
) : Pathfinder<T, R> {

   override fun findShortest(startState: R, end: T): R? = findShortest(startState) { _, t -> t == end }

    fun findShortest(startState: R, endOp: (R, T) -> Boolean): R? {
        add(startState)
        while (toVisit.isNotEmpty()) {
            val state = pick()
            waysOutOp(state)
                .also { if (logging) logWithTime("WaysOut for $state: $it") }
                .forEach { next ->
                    if (endOp(state, next)) {
                        done(adderOp(state, next))
                    } else {
                        add(adderOp(state, next))
                    }
                }

        }

        return currentBest?.first
    }

    private fun add(nextState: R) {
        val distance = distanceOp(nextState)
        if (!meaningfulOp(nextState, distance)) {
            if (logging) logWithTime("skipping $nextState with distance $distance, it's not meaningful")
            return
        }
        val c = currentBest
        if (c == null || c.second > distance) {
            val new = nextState to distance
            toVisit.offer(new)
            if (logging) logWithTime("adding $nextState with distance $distance")
        } else if (logging) logWithTime("skipping $nextState with distance $distance, we got better result already")
    }

    private fun done(nextState: R) {
        val distance = distanceOp(nextState)
        val c = currentBest
        if (c == null || c.second > distance) {
            currentBest = nextState to distance
            if (loggingFound) logWithTime("FOUND $nextState with distance $distance")
        } else if (logging) logWithTime("skipping found $nextState with distance $distance, we got better result already")
    }

    private fun pick(): R {
        val (r, i) = toVisit.poll()
//        if (logging) logWithTime("removing $closest, left $toVisit")
        return r
    }

    private var currentBest: Pair<R, I>? = null
    private val toVisit = PriorityQueue<Pair<R, I>>(priority)
//    private val toVisit: MutableList<Triple<T, R, I>> = mutableListOf()

}

class BasicPathfinder<T : Comparable<T>>(
    logging: Boolean = false,
    adderOp: (List<T>, T) -> List<T> = { l, t -> l + t },
    distanceOp: ((List<T>) -> Int) = { l -> l.size },
    waysOutOp: (List<T>) -> Iterable<T>
) : BFSPathfinder<T, List<T>, Int>(
    logging = logging,
    adderOp = adderOp,
    distanceOp = distanceOp,
    waysOutOp = { l -> waysOutOp(l).filter { it !in l } }
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
    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = op(start, end)
}
