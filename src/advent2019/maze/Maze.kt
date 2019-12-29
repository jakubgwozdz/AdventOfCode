package advent2019.maze

data class Location(val y: Int, val x: Int) : Comparable<Location> {
    override fun toString() = "$y:$x"
    override fun compareTo(other: Location) = comparator.compare(this, other)

    companion object {
        val comparator = compareBy<Location>({ it.y }, { it.x })
    }
}

infix fun Int.yx(x: Int) = Location(this, x)

typealias Delta = Pair<Int, Int>

enum class Direction(val code: Long, val delta: Delta) {
    N(1, -1 to 0),
    S(2, 1 to 0),
    W(3, 0 to -1),
    E(4, 0 to 1)
}

operator fun Location.plus(d: Direction) = this + d.delta
operator fun Location.plus(delta: Delta) = y + delta.first yx x + delta.second
operator fun Location.minus(what: Location) = Direction.values().single { this == what + it }

fun List<Location>.from(pos: Location): List<Direction> {
    var acc = pos
    return map { n -> (n - acc).also { acc = n } }
}

typealias Maze = List<String>

operator fun Maze.get(what: Location) =
    if (what.y in this.indices && what.x in this[what.y].indices) this[what.y][what.x] else null


