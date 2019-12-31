package advent2019.day18

import advent2019.logWithTime
import advent2019.maze.Location
import advent2019.maze.Maze
import advent2019.maze.yx
import advent2019.pathfinder.BFSPathfinder
import advent2019.readAllLines


fun moves(input: List<String>) = moves(Maze(input))

fun moves(maze: Maze): Int {

    maze.input.forEach { logWithTime(it) }
    val mazeAsMap: Map<Location, Char> =
        maze.mapIndexed { y: Int, s: String -> s.mapIndexed { x: Int, c: Char -> (y yx x) to c } }
            .flatten()
            .toMap()
    val start = mazeAsMap.entries.single { (l, c) -> c == '@' }.key
    val keys = (mazeAsMap.filterValues { it.isLowerCase() }).map { (l, c) -> c to l }.toMap()
    val doors = mazeAsMap.filterValues { it.isUpperCase() }.map { (l, c) -> c to l }.toMap()

    logWithTime("start: $start")
    logWithTime("keys: $keys")
    logWithTime("doors: $doors")

    val pois = keys + doors + ('@' to start)
    val paths = pois.map { s ->
        s.key to pois.mapNotNull { e ->
            if (s == e) e.key to 0
            else maze.dist(s.value, e.value)?.let { e.key to it }
        }.toMap()
    }.toMap()

    val bfsPathfinder = BFSPathfinder<Char, List<Path<Char>>, Int>(
        logging = true,
        initialStateOp = { emptyList() },
        adderOp = { l, t ->
            val last = l.lastOrNull()?.e ?: maze[start]!!
            l + Path(last, t, paths[last]!![t]!!)
        },
        distanceOp = { l -> l.sumBy { it.dist } },
        waysOutOp = { l, t ->
            paths[t]!!.keys
                .filter { it != t }
                .filter {
                    !it.isUpperCase() || it.toLowerCase() in l.map { p -> p.e }
                }
        }
    )

    return bfsPathfinder.findShortest('@') { l, t -> (l.map { it.e } + t).containsAll(keys.keys) }!!
        .also { logWithTime(it) }
        .sumBy { it.dist }
}

data class Path<T : Comparable<T>>(val s: T, val e: T, val dist: Int) : Comparable<Path<T>> {

    override fun compareTo(other: Path<T>): Int = compareValuesBy(this, other, { it.dist }, { it.s }, { it.e })

    override fun toString() = "($s->$e, dist=$dist)"

}

fun main() {
    val input = readAllLines("data/input-2019-18.txt")
    moves(Maze(input))
        .also { logWithTime("part 1: $it") }
}
