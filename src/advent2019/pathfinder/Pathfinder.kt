package advent2019.pathfinder

import advent2019.logWithTime

/**
 * T - location
 * D - exits
 * R - state
 */
interface Pathfinder<T : Any, R : Any> {
    fun findShortest(start: T, end: T): R?
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
            .map { nextRoom(newVisited, it) }
            .mapNotNull { next ->
                cache.computeIfAbsent(next, end) { ns, ne -> findShortestProcess(ns, ne, newVisited) }
            }
            .run { if (distanceOp != null) minBy(distanceOp) else minWith(comparator) }
            ?.also { if (logging) logWithTime("found: $it") }
    }
}

open class BFSPathfinder<T:Any, R : Any, I : Comparable<I>>(
    val logging: Boolean=false,
    val loggingFound: Boolean=false,
    val initialStateOp: () -> R,
    val adderOp: (R, T) -> R,
    val distanceOp: ((R) -> I),
    val meaningfulOp: (R, I) -> Boolean = { _, _ -> true },
    val priority: Comparator<Triple<T,R,I>> = compareBy { it.third },
    val waysOutOp: (R, T) -> Iterable<T>
) : Pathfinder<T, R> {

    override fun findShortest(start: T, end: T): R? = findShortest(start) { _, t -> t == end }

    fun findShortest(start: T, endOp: (R, T) -> Boolean): R? {
        add(start, initialStateOp())
        while (toVisit.isNotEmpty()) {
            val (leaf, state) = pick()
            waysOutOp(state, leaf)
                .also { if (logging) logWithTime("WaysOut for $leaf: $it") }
                .forEach { next ->
                    if (endOp(state, next)) {
                        done(next, state)
                    } else {
                        add(next, state)
                    }
                }

        }
        if (logging) logWithTime("best from $start is $currentBest")

        return currentBest?.first
    }

    private fun add(elem: T, prevState: R) {
        val nextState = adderOp(prevState, elem)
        val distance = distanceOp(nextState)
        if (!meaningfulOp(nextState, distance)) {
            if (logging) logWithTime("skipping $nextState with distance $distance, it's not meaningful")
        }
        val c = currentBest
        if (c == null || c.second > distance) {
            val new = Triple(elem, nextState, distance)
            toVisit.add(toVisit.indexOfLast { priority.compare(it,new) < 0 } + 1, new)
            if (logging) logWithTime("adding $nextState with distance $distance")
        } else if (logging) logWithTime("skipping $nextState with distance $distance, we got better result already")
    }

    private fun done(elem: T, prevState: R) {
        val nextState = adderOp(prevState, elem)
        val distance = distanceOp(nextState)
        val c = currentBest
        if (c == null || c.second > distance) {
            currentBest = nextState to distance
            if (loggingFound) logWithTime("FOUND $nextState with distance $distance")
        } else if (logging) logWithTime("skipping found $nextState with distance $distance, we got better result already")
    }

    private fun pick(): Pair<T, R> {
        val closest = toVisit.first()
        if (logging) logWithTime("removing $closest from $toVisit")
        toVisit.remove(closest)
        if (logging) logWithTime("left to check later $toVisit")
        return closest.first to closest.second
    }

    private var currentBest: Pair<R, I>? = null
    private val toVisit: MutableList<Triple<T, R, I>> = mutableListOf()

}

class BasicPathfinder<T : Comparable<T>>(
    logging: Boolean = false,
    initialStateOp: () -> List<T> = { emptyList() },
    adderOp: (List<T>, T) -> List<T> = { l, t -> l + t },
    distanceOp: ((List<T>) -> Int) = { l -> l.size },
    waysOutOp: (List<T>, T) -> Iterable<T>
) : BFSPathfinder<T, List<T>, Int>(
    logging = logging,
    initialStateOp = initialStateOp,
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
    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = op(start, end)
}
