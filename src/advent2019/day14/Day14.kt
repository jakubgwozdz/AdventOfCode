package advent2019.day14

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val input = readAllLines("input-2019-14.txt")
    val reactions = parseReactions(input)

    val one = calculate("ORE", 1 of "FUEL", reactions)
        .also { logWithTime("Part 1: $it") }

    calculateMax("FUEL", 1000000000000 of "ORE", reactions, 1000000000000 / one)
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
                val prodCount = noOfBatches(toProduce, r.product.quantity)
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
        .also { logWithTime("$outputChem require ${rawQuantity of rawMaterial} (test ${++test})") }
}

var test = 0

fun calculateMax(output: Compound, maxStorage: Material, reactions: Reactions, hint: Quantity = 0): Quantity =
    lowestNegative(hint) { quantity ->
        maxStorage.quantity - calculate(maxStorage.compound, quantity of output, reactions)
    } - 1


fun lowestNegative(hint: Quantity = 0, function: (Quantity) -> Quantity): Quantity {

    // initial bounds
    var lowBound = if (hint > 0) 0L to function(0) else hint to function(hint)
    var highBound = if (hint < 0) 0L to function(0) else (hint + 1) to function(hint + 1)

    // widen search range if too narrow
    while (lowBound.second == highBound.second) { // widen initial range if too flat
        val t = (highBound.first - lowBound.first)
        if (lowBound.second < 0) {
            val l = highBound.first - (2 * t)
            lowBound = l to function(l)
        } else {
            val h = lowBound.first + (2 * t)
            highBound = h to function(h)
        }
        logWithTime("Widen to $lowBound - $highBound")
    }

    // naive check for monotonically decreasing
    if (lowBound.second < highBound.second) error("function must be monotonically decreasing")

    // move range down if needed
    while (lowBound.second < 0) {
        val nextTest = interpolate(lowBound, highBound) - 1
        if (nextTest >= lowBound.first) break
        highBound = lowBound
        lowBound = nextTest to function(nextTest)
        logWithTime("Downed to $lowBound - $highBound")
    }

    // move range up if needed
    while (highBound.second >= 0) {
        val nextTest = interpolate(lowBound, highBound) + 1
        if (nextTest <= highBound.first) break
        lowBound = highBound
        highBound = nextTest to function(nextTest)
        logWithTime("Upped to $lowBound - $highBound")
    }

    // narrow range
    while (lowBound.first != highBound.first - 1) {

        val nextTest = interpolate(lowBound, highBound)

        val f = function(nextTest)

        if (f >= 0) {
            lowBound = nextTest to f
        } else {
            highBound = nextTest to f
        }
        logWithTime("Narrowed to $lowBound - $highBound")
    }
    return highBound.first
}

///////// MATH

fun interpolate(l: Pair<Quantity, Quantity>, h: Pair<Quantity, Quantity>): Quantity {
    val (xl, yl) = l
    val (xh, yh) = h
    if (yh == 0L || yl == 0L) return (xl + xh) / 2
    val b = yl - xl * (yh - yl) / (xh - xl)
    val x = -b * (xh - xl) / (yh - yl)
//    return x
    return when {
        x != xl && x != xh -> x // in case of result on
        else -> (xl + xh) / 2
    }
}

fun noOfBatches(toProduce: Quantity, singleProduction: Quantity) =
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
