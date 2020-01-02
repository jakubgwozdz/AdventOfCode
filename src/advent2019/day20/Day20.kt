package advent2019.day20

import advent2019.logWithTime
import advent2019.maze.*
import advent2019.maze.Direction.*
import advent2019.pathfinder.BasicPathfinder
import advent2019.readAllLines

data class Portal(val location: Location, val code: String, val outer: Boolean) : Comparable<Portal> {
    override fun toString(): String = "$code${if (outer) "o" else "i"}"
    override fun compareTo(other: Portal) = comparator.compare(this, other)

    companion object {
        val comparator: Comparator<Portal> = compareBy({ it.code }, { it.location })
    }

}

data class PortalOnLevel(val portal: Portal, val level: Int) : Comparable<PortalOnLevel> {
    override fun toString(): String = "$portal@$level"
    override fun compareTo(other: PortalOnLevel): Int {
        return portal.compareTo(other.portal).let { if (it == 0) level.compareTo(other.level) else it }
    }
}

data class Connection(
    val portal1: Portal,
    val portal2: Portal,
    val distance: Int,
    val levelChange: Int
) : Comparable<Connection> {
    override fun toString(): String = "$portal1->$portal2=$distance${when {
        levelChange < 0 -> "⇓"
        levelChange > 0 -> "⇑"
        else -> "↔"
    }}"

    override fun compareTo(other: Connection) = comparator.compare(this, other)

    companion object {
        val comparator: Comparator<Connection> = compareBy({ it.portal1 }, { it.portal2 })
    }
}

data class ConnectionOnLevel(
    val portal1: PortalOnLevel,
    val portal2: PortalOnLevel,
    val distance: Int,
    val levelChange: Int = portal2.level - portal1.level
) : Comparable<ConnectionOnLevel> {
    override fun toString(): String = "$portal1->$portal2=$distance${when {
        levelChange < 0 -> "⇓"
        levelChange > 0 -> "⇑"
        else -> "↔"
    }}"

    override fun compareTo(other: ConnectionOnLevel) = comparator.compare(this, other)

    companion object {
        val comparator: Comparator<ConnectionOnLevel> =
            compareBy({ it.portal1.level }, { it.portal1.portal }, { it.portal2.portal })
    }
}

class Donut(val maze: Maze) {

    constructor(input: List<String>) : this(Maze(input))

    val portals: Set<Portal> by lazy { findPortals(maze) }
    val roads: Set<Connection> by lazy { findConnections(maze, portals, false) }

    fun shortest(
        start: Portal = this.start,
        end: Portal = this.end,
        logging: Boolean = false
    ): List<Connection> {
        val pathfinder = BasicPathfinder<Portal>(
            logging = logging,
            distanceOp = {
                connectionsBetweenPortals(it)
                    .also { if (logging) logWithTime("Calculating $it") }
                    .sumBy(Connection::distance)
            },
            waysOutOp = { l ->
                regularLeafs(l)
            }
        )

        val shortest = pathfinder.findShortest(listOf(start), end)
            ?.let { connectionsBetweenPortals(it) }
            .also { if (logging) logWithTime("AA->ZZ: found $it") }
        return shortest?.toList() ?: error("Not found")
    }

    fun regularLeafs(l: List<Portal>): List<Portal> = (connectionsCache[l.last()] ?: emptyMap())
        .values
        .asSequence()
        .map { it.portal2 }
        .filter { testEverySecondIsPortal(l, it) { p -> p } }
        .toList()

    fun recursiveLeafs(l: List<PortalOnLevel>): Iterable<PortalOnLevel> {
        if (l.size > 250) return emptyList() // IKR
        return (connectionsCache[l.last().portal] ?: emptyMap())
            .values
            .asSequence()
            .filter { testEverySecondIsPortal(l, it.portal2) { t -> t.portal } }
            .filter { (it.portal2.code != "AA" && it.portal2.code != "ZZ") || l.last().level == 0 }
            .map { PortalOnLevel(it.portal2, l.last().level + it.levelChange) }
            .filter { it.level <= 0 }
            .toList()
    }

    private fun <T> testEverySecondIsPortal(l: List<T>, p: Portal, op: (T) -> Portal): Boolean {
        val li = l.size - 1
        return l.size < 2 || (op(l[li]).code == p.code) != (op(l[li - 1]).code == op(l[li]).code)
    }

    fun shortestRecursive(
        start: PortalOnLevel = PortalOnLevel(this.start, 0),
        end: PortalOnLevel = PortalOnLevel(this.end, 0),
        logging: Boolean = false
    ): List<ConnectionOnLevel> {

        val pathfinder = BasicPathfinder<PortalOnLevel>(
            logging = logging,
            distanceOp = {
                connectionsBetweenPortalsOnLevel(it)
                    .also { if (logging) logWithTime("Calculating $it") }
                    .sumBy(ConnectionOnLevel::distance)
            },
            waysOutOp = { l ->
                recursiveLeafs(l)
            }
        )

        val shortest = pathfinder.findShortest(listOf(start), end)
            ?.let { connectionsBetweenPortalsOnLevel(it) }
            .also { if (logging) logWithTime("AA->ZZ: found $it") }
        return shortest?.toList() ?: error("Not found")
    }

    private val connectionsCache by lazy {
        roads.groupBy(Connection::portal1)
            .mapValues { (_, l) -> l.groupBy(Connection::portal2).mapValues { (_, l) -> l.single() } }
    }

    fun connectionsBetweenPortals(portals: List<Portal>): MutableList<Connection> {
        val result = mutableListOf<Connection>()
        var prev = portals.first()
        portals.drop(1).forEach { result += connectionsCache[prev]!![it]!!; prev = it }
        return result
    }

    fun connectionsBetweenPortalsOnLevel(portals: List<PortalOnLevel>): MutableList<ConnectionOnLevel> {
        val result = mutableListOf<ConnectionOnLevel>()
        var prev = portals.first()
        portals.drop(1).forEach {
            val distance = connectionsCache[prev.portal]!![it.portal]!!.distance
            result += ConnectionOnLevel(prev, it, distance)
            prev = it
        }
        return result
    }

    val start get() = portals.single { it.code == "AA" }
    val end get() = portals.single { it.code == "ZZ" }

}


private fun findPortals(maze: Maze): Set<Portal> {
    return maze
        .mapIndexed { y, s -> s.mapIndexed { x, c -> (y yx x) to c } }
        .flatten()
        .filter { it.second == '.' }
        .flatMap { (l: Location, _) -> findPortalsForLocation(maze, l) }
        .toSet()
}

private fun findPortalsForLocation(
    maze: Maze,
    l: Location
): List<Portal> {
    return values()
        .filter { maze[l + it]?.isUpperCase() ?: false }
        .map {
            it to when (it) {
                N -> "${maze[l + it + it]}${maze[l + it]}"
                S -> "${maze[l + it]}${maze[l + it + it]}"
                W -> "${maze[l + it + it]}${maze[l + it]}"
                E -> "${maze[l + it]}${maze[l + it + it]}"
            }
        }
        .map { (d, c) ->
            val outer = when (d) {
                N -> l.y < maze.size / 2
                S -> l.y > maze.size / 2
                W -> l.x < maze[l.y].length / 2
                E -> l.x > maze[l.y].length / 2
            }
            Portal(l, c, outer)
        }
}


private fun findConnections(maze: Maze, portals: Set<Portal>, logging: Boolean = false): Set<Connection> {

    return portals.flatMap { start -> portals.map { end -> start to end } }
        .filter { (start, end) -> start.location != end.location }
        .mapNotNull { (start, end) ->
            if (logging) logWithTime("Testing $start->$end")

            val pathfinder = BasicPathfinder<Location>(logging) { l ->
                Direction.values().map { l.last() + it }.filter { maze[it] == '.' }
            }

            val shortestPath = if (start.code == end.code) listOf(start, end)
            else pathfinder.findShortest(listOf(start.location), end.location)

            shortestPath?.let {
                if (logging) logWithTime("path $start(${start.location})->$end(${end.location}) is $it")
                Connection(
                    start, end, it.size - 1,
                    when {
                        start.code == end.code && start.outer && !end.outer -> 1
                        start.code == end.code && !start.outer && end.outer -> -1
                        else -> 0
                    }
                )
            }
                .also { if (logging) logWithTime("$start->$end = $it") }
        }
        .toSet()
}


fun main() {
    val input = readAllLines("data/input-2019-20.txt")
        .also { logWithTime("Maze size: ${it.sumBy(String::length)}") }
    val donut = Donut(input)

    val part1 = donut
        .also { logWithTime("Portals: ${it.portals.sorted()}") }
        .shortest(logging = false)
        .also { logWithTime("shortest path is $it") }
        .sumBy(Connection::distance)

    logWithTime("shortest path length is $part1")

    val part2 = donut
        .also { logWithTime("Portals: ${it.portals.sorted()}") }
        .shortestRecursive(logging = false)
        .also { logWithTime("shortest recursive path is $it") }
        .sumBy(ConnectionOnLevel::distance)

    logWithTime("shortest recursive path length is $part2")
}
