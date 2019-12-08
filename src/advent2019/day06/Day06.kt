package advent2019.day06

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val regex = Regex("(\\w+)\\)(\\w+)")
    val orbits = readAllLines("input-2019-06.txt")
        .map { regex.matchEntire(it) ?: error("syntax at $it") }
        .map { it.destructured.component2() to it.destructured.component1() }
        .toMap()
    logWithTime("read ${orbits.size} orbits")
    val distances = orbits.mapValues { (s: String, _) -> countRelations(s, orbits) }
    logWithTime("calculated ${distances.size} indirections")
    val count = distances.values.sum()
    logWithTime("result is $count")

    var youOrbit = orbits["YOU"]!!
    var sanOrbit = orbits["SAN"]!!
    var jumps = 0

    while (youOrbit != sanOrbit) {
        jumps += 1
        if (distances[youOrbit]!! > distances[sanOrbit]!!) {
            val newOrbit = orbits[youOrbit]!!
            logWithTime("$jumps: YOU from $youOrbit to $newOrbit")
            youOrbit = newOrbit
        } else {
            val newOrbit = orbits[sanOrbit]!!
            logWithTime("$jumps: SAN from $sanOrbit to $newOrbit")
            sanOrbit = newOrbit
        }
    }
}


fun countRelations(
    satellite: String,
    orbits: Map<String, String>,
    cache: MutableMap<String, Int> = mutableMapOf()
): Int {
    return cache[satellite] ?: calculate(satellite, orbits, cache).also { cache[satellite] = it }
}

private fun calculate(
    satellite: String,
    orbits: Map<String, String>,
    cache: MutableMap<String, Int>
) = orbits[satellite]?.let { parent ->
    1 + (cache[parent] ?: countRelations(parent, orbits, cache))
} ?: 0
