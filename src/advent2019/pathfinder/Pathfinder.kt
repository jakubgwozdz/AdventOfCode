package advent2019.pathfinder

import advent2019.logWithTime

interface Pathfinder<T : Any, R : Any> {
    fun findShortest(start: T, end: T): R?
}

open class DFSPathfinder<T : Any, R : Any>(
    val logging: Boolean,
    val cache: Cache<T, R>,
    val initialStateOp: () -> R,
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
                cache.computeIfAbsent(next, end) { ns, ne -> findShortestProcess(ns, ne, newVisited) }
            }
            .run { if (distanceOp != null) minBy(distanceOp) else minWith(comparator) }
            ?.also { if (logging) logWithTime("found: $it") }
    }
}

open class BFSPathfinder<T : Any, R : Any, I : Comparable<I>>(
    val logging: Boolean,
    val initialStateOp: () -> R,
    val adderOp: (R, T) -> R,
    val distanceOp: ((R) -> I),
//    val comparator: Comparator<R> = distanceOp?.let { compareBy(it) } ?: error("Requires distanceOp or comparator"),
    val waysOutOp: (R, T) -> Iterable<T>
) : Pathfinder<T, R> {

    override fun findShortest(start: T, end: T): R? {
        add(start, initialStateOp())
        while (toVisit.isNotEmpty()) {
            val (leaf, state) = pick()
            waysOutOp(state, leaf)
                .also { if (logging) logWithTime("WaysOut for $leaf: $it") }
                .forEach { next ->
                    if (next == end) {
                        done(next, state)
                    } else {
                        add(next, state)
                    }
                }

        }
        if (logging) logWithTime("best $start->$end is $currentBest")

        return currentBest?.first
    }

    private fun add(elem: T, prevState: R) {
        val nextState = adderOp(prevState, elem)
        val distance = distanceOp(nextState)
        val c = currentBest
        if (c == null || c.second > distance) {
            toVisit.add(
                Triple(elem, nextState, distance)
            )
            if (logging) logWithTime("adding $nextState with distance $distance")
        } else if (logging) logWithTime("skipping $nextState with distance $distance, we got better result already")
    }

    private fun done(elem: T, prevState: R) {
        val nextState = adderOp(prevState, elem)
        val distance = distanceOp(nextState)
        val c = currentBest
        if (c == null || c.second > distance) {
            currentBest = nextState to distance
            if (logging) logWithTime("FOUND $nextState with distance $distance")
        } else if (logging) logWithTime("skipping found $nextState with distance $distance, we got better result already")
    }

    private fun pick(): Pair<T, R> {
        val closest = toVisit.minBy { it.third }!!
        if (logging) logWithTime("removing $closest from $toVisit")
        toVisit.remove(closest)
        if (logging) logWithTime("left to check later $toVisit")
        return closest.first to closest.second
    }

    private var currentBest: Pair<R, I>? = null
    private val toVisit: MutableCollection<Triple<T, R, I>> = mutableListOf()

}

class BasicPathfinder<T : Any>(
    logging: Boolean = false,
    cache: Cache<T, List<T>> = NoCache(),
    initialStateOp: () -> List<T> = { emptyList() },
    adderOp: (List<T>, T) -> List<T> = { l, t -> l + t },
    distanceOp: ((List<T>) -> Int) = { l -> l.size },
//    comparator: Comparator<List<T>> = distanceOp?.let { compareBy(it) } ?: error("Requires distanceOp or comparator"),
    waysOutOp: (List<T>, T) -> Iterable<T>
//) : DFSPathfinder<T, List<T>>(
) : BFSPathfinder<T, List<T>, Int>(
    logging = logging,
//    logging = true,
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
    override fun computeIfAbsent(start: T, end: T, op: (T, T) -> List<D>?): List<D>? = when (start) {
//        end -> error("checking for $start<->$end")
        else -> op(start, end)
    }
}
