package advent2019.maze

import advent2019.pathfinder.BasicPathfinder

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

data class Maze(val input: List<String>) {
    val size = input.size
    fun mapIndexed(function: (Int, String) -> List<Pair<Location, Char>>) = input.mapIndexed(function)

    operator fun get(y: Int) = input[y]

    operator fun get(what: Location) =
        if (what.y in this.input.indices && what.x in this.input[what.y].indices) this.input[what.y][what.x] else null

    fun dist(s: Location, e: Location): Int? {
        return BasicPathfinder<Location> { l, t ->
            Direction.values()
                .map { t + it }
                .filter { t1 -> t1 == e || this[t1] == '.' }
        }.findShortest(s, e)
            ?.let { it.size - 1 }
    }

}
