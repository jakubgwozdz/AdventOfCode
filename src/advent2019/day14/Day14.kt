package advent2019.day14

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val input = readAllLines("data/input-2019-14.txt")
    val reactions = parseReactions(input)

    calculate("ORE", 1 of "FUEL", reactions)
        .also { logWithTime("Part 1: $it") }

    calculateMax("FUEL", 1000000000000 of "ORE", reactions)
        .also { logWithTime("Part 2: $it") }

}



typealias Compound = String
typealias Quantity = Long

infix fun Int.of(compound: Compound): Material = Material(this.toLong(), compound)
infix fun Quantity.of(compound: Compound): Material = Material(this, compound)

data class Material(val quantity: Quantity, val compound: Compound) {
    override fun toString(): String = "$quantity $compound"
}

data class Reaction(val reagents: List<Material>, val product: Material) {
    override fun toString(): String = "{ $reagents => $product }"
}

typealias Reactions = Map<Compound, Reaction>

fun calculate(
    rawMaterial: Compound,
    outputChem: Material,
    reactions: Reactions
): Quantity {
    val leftovers = mutableMapOf<Compound, Quantity>()
    val needs = mutableMapOf(outputChem.compound to outputChem.quantity)
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
                val prodCount = productionsCount(toProduce, r.product.quantity)
                r.reagents.forEach {
                    val q = it.quantity * prodCount
                    needs[it.compound] = (needs[it.compound] ?: 0) + q
                }
                // leftovers not needed
                leftovers[name] = (leftovers[name] ?: 0) + ((r.product.quantity * prodCount) - toProduce)
            }
        } else {
            leftovers[name] = inStorage - quantity
        }
    }

    return rawQuantity
        .also { logWithTime("$outputChem require ${rawQuantity to rawMaterial}")}
}

fun calculateMax(output: Compound, maxStorage: Material, reactions: Reactions): Quantity =
    highestThat { quantity -> calculate(maxStorage.compound, quantity of output, reactions) <= maxStorage.quantity }

fun highestThat(predicate: (Quantity) -> Boolean): Quantity {

    var lowBound = -1L
    var highBound = 1L

    while (predicate(highBound)) {
        lowBound = highBound; highBound = (highBound + 1) * 2
    }
    while (!predicate(lowBound)) {
        highBound = lowBound; lowBound = (lowBound - 1) * 2
    }

    while (lowBound < highBound - 1) {
        val test = (lowBound + highBound) / 2
        if (predicate(test)) {
            lowBound = test
        } else {
            highBound = test
        }
    }
    return lowBound
}

fun productionsCount(toProduce: Quantity, singleProduction: Quantity) =
    if (toProduce <= 0) 0 else (toProduce - 1) / singleProduction + 1


///////// PARSING

val regex = Regex("\\s*(\\d+)\\s+(\\w+)\\s*")

fun parseChemical(s: String): Material {
    return regex.matchEntire(s)
        ?.let { Material(it.destructured.component1().toLong(), it.destructured.component2()) }
        ?: error("$s not chemical")
}

// 7 A, 1 E => 1 FUEL
fun parseReaction(line: String): Reaction {
    return line.split("=>")
        .let { it[0].split(",") to it[1] }
        .let { Reaction(it.first.map { c -> parseChemical(c) }, parseChemical(it.second)) }
}

fun parseReactions(lines: List<String>): Reactions {
    return lines.map { parseReaction(it) }.associateBy { it.product.compound }
}
