package advent2019.day20

import advent2019.logWithTime
import advent2019.maze.*
import advent2019.maze.Direction.*
import advent2019.readAllLines

data class Portal(val location: Location, val code: String, val outer: Boolean) : Comparable<Portal> {
    override fun toString(): String = "$code:$location:${if (outer) "Ȯ" else "ⱺ"}"
    override fun compareTo(other: Portal) = comparator.compare(this, other)

    companion object {
        val comparator: Comparator<Portal> = compareBy({ it.code }, { it.location })
    }

    fun levelChangeFrom(other: Portal): Int {
        return if (outer == other.outer || code == other.code) 0 else if (outer) -1 else 1
    }
}

data class Connection(
    val portal1: Portal,
    val portal2: Portal,
    val distance: Int,
    val levelChange: Int = portal2.levelChangeFrom(portal1)
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

class Donut(val maze: Maze) {

    val portals: Set<Portal> by lazy { findPortals(maze) }
    val roads: Set<Connection> by lazy { findConnections(maze, portals, false) }

    fun shortest(
        start: Portal = this.start,
        end: Portal = this.end,
        logging: Boolean = false
    ): List<Connection> {

        val shortest = shortest<Portal, List<Portal>>(
            start = start,
            end = end,
            logging = logging,
            cache = NoCache(logging),
            visited = emptyList(),
            accOp = { listOf(start) },
            adderOp = { l, e -> l + e },
            selector = {
                connectionsBetweenPortals(it).also { if (logging) logWithTime("Calculating $it") }
                    .sumBy(Connection::distance)
            },
            waysOutOp = { p ->
                roads
                    .filter { p == it.portal1 }
                    .map { it.portal2 }
                    .also { if (logging) logWithTime("WaysOut for $p: $it") }
            }
        )
            ?.let { connectionsBetweenPortals(it) }
            .also { if (logging) logWithTime("AA->ZZ: found") }
        return shortest?.toList() ?: error("Not found")
    }

    fun shortestRecursive(
        start: Portal = this.start,
        end: Portal = this.end,
        logging: Boolean = false
    ): List<Connection> {

        val shortest = shortest<Portal, List<Portal>>(
            start = start,
            end = end,
            logging = logging,
            cache = NoCache(logging),
            visited = emptyList(),
            accOp = { listOf(start) },
            adderOp = { l, e -> l + e },
            selector = {
                connectionsBetweenPortals(it).also { if (logging) logWithTime("Calculating $it") }
                    .sumBy(Connection::distance)
            },
            waysOutOp = { p ->
                roads
                    .filter { p == it.portal1 }
                    .map { it.portal2 }
                    .also { if (logging) logWithTime("WaysOut for $p: $it") }
            }
        )
            ?.let { connectionsBetweenPortals(it) }
            .also { if (logging) logWithTime("AA->ZZ: found") }
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
            val shortestPath = if (start.code == end.code) listOf(start, end) else (
                    shortestPath(
                        start.location,
                        end.location,
                        logging = logging
                    ) { p -> Direction.values().map { p + it }.filter { maze[it] == '.' } })
            shortestPath?.let { Connection(start, end, it.size - 1) }
                .also { if (logging) logWithTime("$start->$end = $it") }
        }
        .toSet()
}


fun main() {
    val input = readAllLines("input-2019-20.txt")
        .also { logWithTime("Maze size: ${it.sumBy(String::length)}") }
    val donut = Donut(input)

    donut
        .also { logWithTime("Portals: ${it.portals.sorted()}") }
        .shortest(logging = true).also { logWithTime("shortest path is $it") }
        .let { it.sumBy(Connection::distance) }
        .also { logWithTime("shortest path length is $it") }
}