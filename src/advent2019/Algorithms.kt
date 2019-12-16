package advent2019

import kotlin.math.absoluteValue

fun permutations(n: Int): List<List<Int>> {
    val a: IntArray = (0 until n).toList().toIntArray()
    val result = mutableListOf<List<Int>>()
    generate(n, a) { result.add(it.toList()) }
    return result.toList()
}

fun generate(k: Int, a: IntArray, outputOp: (IntArray) -> Unit) {
    if (k == 1) outputOp.invoke(a)
    else {
        generate(k - 1, a, outputOp)
        (0 until k - 1)
            .forEach { i ->
                if (k % 2 == 0)
                    a.swap(i, k - 1)
                else
                    a.swap(0, k - 1)
                generate(k - 1, a, outputOp)
            }
    }
}

fun IntArray.swap(i: Int, j: Int) {
    val v = this[i]
    this[i] = this[j]
    this[j] = v
}

tailrec fun gcd(a: Int, b: Int): Int {
    return when (a) {
        0 -> b.absoluteValue
        else -> gcd(b.absoluteValue % a.absoluteValue, a.absoluteValue)
    }
}

tailrec fun gcd(a: Long, b: Long): Long {
    return when (a) {
        0L -> b.absoluteValue
        else -> gcd(b.absoluteValue % a.absoluteValue, a.absoluteValue)
    }
}

fun lcm(a: Int, b: Int): Int {
    return lcm(a.toLong(), b.toLong()).toInt()
}

fun lcm(a: Long, b: Long): Long {
    return a * b / gcd(a, b)
}

