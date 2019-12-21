package advent2019.day20

import advent2019.logWithTime
import advent2019.maze.*
import advent2019.maze.Direction.*
import advent2019.pathfinder.BasicPathfinder
import advent2019.pathfinder.PathCache
import advent2019.readAllLines

data class Portal(val location: Location, val code: String, val outer: Boolean) : Comparable<Portal> {
    override fun toString(): String = "$code${if (outer) "o" else "i"}"
    override fun compareTo(other: Portal) = comparator.compare(this, other)

    companion object {
        val comparator: Comparator<Portal> = compareBy({ it.code }, { it.location })
    }

}

data class PortalOnLevel(val portal: Portal, val level: Int) {
    override fun toString(): String = "$portal@$level"
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

    val portals: Set<Portal> by lazy { findPortals(maze) }
    val roads: Set<Connection> by lazy { findConnections(maze, portals, false) }

    fun shortest(
        start: Portal = this.start,
        end: Portal = this.end,
        logging: Boolean = false
    ): List<Connection> {
        val pathfinder = BasicPathfinder<Portal>(
            logging = logging,
            comparator = compareBy {
                connectionsBetweenPortals(it)
                    .also { if (logging) logWithTime("Calculating $it") }
                    .sumBy(Connection::distance)
            },
            waysOutOp = { l, p ->
                regularLeafs(l, p, logging)
            }
        )

        val shortest = pathfinder.findShortest(start, end)
            ?.let { connectionsBetweenPortals(it) }
            .also { if (logging) logWithTime("AA->ZZ: found $it") }
        return shortest?.toList() ?: error("Not found")
    }

    fun regularLeafs(l: List<Portal>, p: Portal, logging: Boolean): List<Portal> {
        return roads
            .filter { p == it.portal1 }
            .map { it.portal2 }
//            .filter {
//                l.size < 2 ||
//                        (l.takeLast(2).first().code == l.last().code
//                                && l.last().code != it.portal2.code) ||
//                        (l.takeLast(2).first().code != l.last().code
//                                && l.last().code == it.portal2.code)
//            }
            .also { if (logging) logWithTime("WaysOut for $p: $it") }
    }


    fun shortestRecursive(
        start: PortalOnLevel = PortalOnLevel(this.start, 0),
        end: PortalOnLevel = PortalOnLevel(this.end, 0),
        logging: Boolean = false
    ): List<ConnectionOnLevel> {

        val pathfinder = BasicPathfinder<PortalOnLevel>(
            logging = logging,
            stopOp = { visited, p -> stopOp(visited, p) },
            comparator = compareBy {
                connectionsBetweenPortalsOnLevel(it)
                    .also { if (logging) logWithTime("Calculating $it") }
                    .sumBy(ConnectionOnLevel::distance)
            },
            waysOutOp = { l, p ->
                roads
                    .filter { p.portal == it.portal1 }
                    .filter {
                        // test every second is via portal
                        l.size < 2 || (l[0].portal.code == it.portal2.code) != (l[1].portal.code == l[0].portal.code)
                    }
                    .filter { (it.portal2.code != "AA" && it.portal2.code != "ZZ") || p.level == 0 }
                    .map { PortalOnLevel(it.portal2, p.level + it.levelChange) }
                    .filter { it.level <= 0 }
                    .also { if (logging) logWithTime("WaysOut for $p, $l: $it") }
            }
        )

        val shortest = pathfinder.findShortest(start, end)
            ?.let { connectionsBetweenPortalsOnLevel(it) }
            .also { if (logging) logWithTime("AA->ZZ: found $it") }
        return shortest?.toList() ?: error("Not found")
    }

    private fun stopOp(visited: List<PortalOnLevel>, t: PortalOnLevel): Boolean {
        // IKR
        return t in visited || (visited.size > 250).also {
            //            if(it) { println(visited.map { pl-> pl.portal.code } ) ; exitProcess(-1)}
        }
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

            val pathfinder = BasicPathfinder<Location>(false, PathCache(logging)) { _, p ->
                Direction.values().map { p + it }.filter { maze[it] == '.' }
            }

            val shortestPath = if (start.code == end.code) listOf(start, end)
            else pathfinder.findShortest(start.location, end.location)

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
    val input = readAllLines("input-2019-20.txt")
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