package advent2019.day06

import advent2019.log
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val regex = Regex("(\\w+)\\)(\\w+)")
    val orbits = Files.readAllLines(Paths.get("input-2019-06.txt"))
        .map { regex.matchEntire(it) ?: error("syntax at $it") }
        .map { it.destructured.component2() to it.destructured.component1() }
        .toMap()
    log("read ${orbits.size} orbits")
    val distances = orbits.mapValues { (s: String, _) -> countRelations(s, orbits) }
    log("calculated ${distances.size} indirections")
    val count = distances.values.sum()
    log("result is $count")

    var youOrbit = orbits["YOU"]!!
    var sanOrbit = orbits["SAN"]!!
    var jumps = 0

    while (youOrbit != sanOrbit) {
        jumps += 1
        if (distances[youOrbit]!! > distances[sanOrbit]!!) {
            val newOrbit = orbits[youOrbit]!!
            log("$jumps: YOU from $youOrbit to $newOrbit")
            youOrbit = newOrbit
        } else {
            val newOrbit = orbits[sanOrbit]!!
            log("$jumps: SAN from $sanOrbit to $newOrbit")
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
