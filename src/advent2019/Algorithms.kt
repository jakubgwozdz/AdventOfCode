package advent2019

import kotlin.math.absoluteValue


fun permutations(n: Int): List<List<Int>> {
    val a: IntArray = (0 until n).toList().toIntArray()
    val result = mutableListOf<List<Int>>()
    generatePermutation(n, a) { result.add(it.toList()) }
    return result.toList()
}

fun generatePermutation(k: Int, a: IntArray, outputOp: (IntArray) -> Unit) {
    if (k == 1) outputOp.invoke(a)
    else {
        generatePermutation(k - 1, a, outputOp)
        (0 until k - 1)
            .forEach { i ->
                if (k % 2 == 0)
                    a.swap(i, k - 1)
                else
                    a.swap(0, k - 1)
                generatePermutation(k - 1, a, outputOp)
            }
    }
}

fun permutationsWithRepetitions(k: Int, n: Int): Sequence<List<Int>> {
    val a = IntArray(n) { 0 }
    var start = true
    return generateSequence {
        if (start) {
            start = false
            a.toList()
        } else {
            var i = 0
            a[i]++
            while (a[i] >= k) {
                a[i] = 0
                i++
                if (i >= n) return@generateSequence null
                a[i]++
            }
            a.toList()
        }
    }
}

//var permutations = IntArray(N)
//fun addOne(): Boolean { // Returns true when it advances, false _once_ when finished
//    var i = 0
//    permutations.get(i)++
//    while (permutations.get(i) >= K) {
//        permutations.get(i) = 0
//        i += 1
//        if (i >= N) return false
//        permutations.get(i)++
//    }
//    return true
//}
//


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

