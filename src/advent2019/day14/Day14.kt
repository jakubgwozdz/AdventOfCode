package advent2019.day14

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val input = readAllLines("input-2019-14.txt")
    val reactions = parseReactions(input)

    calculate("ORE", 1L to "FUEL", reactions)
        .also { logWithTime("Part 1: $it") }

    calculateMax("FUEL", 1000000000000L to "ORE", reactions)
        .also { logWithTime("Part 2: $it") }

}



typealias Name = String
typealias Quantity = Long
typealias Chemical = Pair<Quantity, Name>

data class Reaction(val reagents: List<Chemical>, val product: Chemical) {
    override fun toString(): String = "{ $reagents => $product }"
}

typealias Reactions = Map<Name, Reaction>

fun calculate(
    rawMaterial: Name,
    outputChem: Chemical,
    reactions: Reactions
): Quantity {
    val leftovers = mutableMapOf<Name, Quantity>()
    val needs = mutableMapOf(outputChem.second to outputChem.first)
    var rawQuantity: Quantity = 0

    while (needs.isNotEmpty()) {
        val name = needs.keys.first()
        val quantity = needs.remove(name) ?: error("$name ???")

        val inStorage = leftovers[name] ?: 0

        if (quantity > inStorage) {
            leftovers[name] = 0 // taking all from storage
            val toProduce = quantity - inStorage
            if (name == rawMaterial) {
                rawQuantity += toProduce
            } else {
                val r = reactions[name] ?: error("unknown reagent $name")
                val prodCount = productionsCount(toProduce, r.product.first)
                r.reagents.forEach {
                    val q = it.first * prodCount
                    needs[it.second] = (needs[it.second] ?: 0) + q
                }
                // leftovers not needed
                leftovers[name] = (leftovers[name] ?: 0) + ((r.product.first * prodCount) - toProduce)
            }
        } else {
            leftovers[name] = inStorage - quantity
        }
    }

    return rawQuantity
}

fun calculateMax(output: Name, maxStorage: Chemical, reactions: Reactions): Quantity =
    maxUsingTries { calculate(maxStorage.second, it to output, reactions) <= maxStorage.first }

fun maxUsingTries(predicate: (Quantity) -> Boolean): Long {

    var lowBound = -1L
    var highBound = 1L

    while (predicate(highBound)) highBound = (highBound + 1) * 2
    while (!predicate(lowBound)) lowBound = (lowBound - 1) * 2

    do {
        val test = (lowBound + highBound) / 2
        if (predicate(test)) {
            lowBound = test
        } else {
            highBound = test
        }
    } while (lowBound < highBound - 1)
    return lowBound
}

fun productionsCount(toProduce: Quantity, singleProduction: Quantity) =
    if (toProduce <= 0) 0 else (toProduce - 1) / singleProduction + 1


///////// PARSING

val regex = Regex("\\s*(\\d+)\\s+(\\w+)\\s*")

fun parseChemical(s: String): Chemical {
    return regex.matchEntire(s)
        ?.let { Chemical(it.destructured.component1().toLong(), it.destructured.component2()) }
        ?: error("$s not chemical")
}

// 7 A, 1 E => 1 FUEL
fun parseReaction(line: String): Reaction {
    return line.split("=>")
        .let { it[0].split(",") to it[1] }
        .let { Reaction(it.first.map { c -> parseChemical(c) }, parseChemical(it.second)) }
}

fun parseReactions(lines: List<String>): Reactions {
    return lines.map { parseReaction(it) }.associateBy { it.product.second }
}
