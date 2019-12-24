package advent2019.day10

import advent2019.logWithTime
import advent2019.readAllLines
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.sign

fun main() {
    val lines = readAllLines("data/input-2019-10.txt")
        .also { logWithTime("read ${it.size} lines") }
    var asteroids = parseAsteroids(lines)
        .also { logWithTime("found ${it.size} asteroids") }
    val bestLocation = findBestLocation(asteroids)
        .also { logWithTime("best is ${it.first} with ${it.second.size} in line of sight") }
    val laser = bestLocation.first
    var toVaporize = bestLocation.second

    val vaporized = mutableListOf<Location>()

    while (vaporized.size + toVaporize.size < 200) {
        logWithTime("vaporizing all visible ${toVaporize.size}")
        vaporized += toVaporize
        asteroids -= toVaporize
        toVaporize = findVisible(laser, asteroids)
    }

    toVaporize
        .sortedBy { (laser - it).angle }
        .get(199 - vaporized.size)
        .also { logWithTime("200th vaporized is $it -> ${it.second * 100 + it.first}") }

}

typealias Location = Pair<Int, Int>
typealias Vector = Pair<Int, Int>

fun parseAsteroids(lines: List<String>): Collection<Location> {
    return lines
        .mapIndexed { i, l -> l.mapIndexedNotNull { j, c -> if (c == '#') i to j else null } }
        .flatten()
}


fun findBestLocation(asteroids: Collection<Location>): Pair<Location, List<Location>> = asteroids
    .map { a -> a to findVisible(a, asteroids) }
    .maxBy { (_, l) -> l.size }!!

fun findVisible(a: Location, asteroids: Collection<Location>) = asteroids
    .mapNotNull { b -> if (a.canSee(b, asteroids)) b else null }

fun Location.canSee(that: Location, asteroids: Collection<Location>): Boolean {
    if (this == that) return false
    val vector = that - this
    return asteroids
        .map { it - this }
        .none { it.blocks(vector) }
}

operator fun Location.minus(that: Location): Vector =
    that.first - this.first to that.second - this.second

val Vector.angle: Double
    get() = atan2(second.toDouble(), -first.toDouble()).let { if (it >= 0.0) it else it + PI + PI }

fun Vector.blocks(that: Vector): Boolean {
    return if (this == that) false
    else this.second * that.first == this.first * that.second &&
            this.first.sign == that.first.sign &&
            this.second.sign == that.second.sign &&
            this.first.absoluteValue <= that.first.absoluteValue &&
            this.second.absoluteValue <= that.second.absoluteValue
}
