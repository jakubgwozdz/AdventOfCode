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
    val acc = mutableMapOf<String, Int>()
    orbits.keys.forEach { s: String ->
        countRelations(s, acc, orbits)
    }
    log("calculated ${acc.size} indirections")
    val count = acc.values.sum()
    log("result is $count")

    var youOrbit = orbits["YOU"]!!
    var sanOrbit = orbits["SAN"]!!
    var jumps = 0

    while (youOrbit != sanOrbit) {
        jumps += 1
        if (acc[youOrbit]!! > acc[sanOrbit]!!) {
            val newOrbit = orbits[youOrbit]!!
            log("$jumps: YOU from $youOrbit to $newOrbit")
            youOrbit = newOrbit
        } else {
            val newOrbit = orbits[sanOrbit]!!
            log("$jumps: SAN from $youOrbit to $newOrbit")
            sanOrbit = newOrbit
        }
    }

}


fun countRelations(s: String, acc: MutableMap<String, Int>, orbits: Map<String, String>) {
    assert(acc[s] == null)
    val parent = orbits[s]
    if (parent == null) {
        acc[s] = 0 // center
    } else {
        if (acc[parent] == null) countRelations(parent, acc, orbits)
        acc[s] = acc[parent]!! + 1
    }
}
