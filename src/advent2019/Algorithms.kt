package advent2019

import kotlin.math.absoluteValue


fun permutations(n: Int): Sequence<List<Int>> {
    val a = IntArray(n) { it }
    return sequenceOf(a.toList()) + generateSequence {
        var i = a.size - 1
        while (i > 0 && a[i] <= a[i - 1]) i--
        if (i <= 0) return@generateSequence null
        var j = a.size - 1
        while (a[j] <= a[i - 1]) j--
        a.swap(i - 1, j)
        j = a.size - 1
        while (i < j) a.swap(i++, j--)
        a.toList()
    }
}

fun permutationsWithRepetitions(k: Int, n: Int): Sequence<List<Int>> {
    val a = IntArray(n) { 0 }
    return sequenceOf(a.toList()) + generateSequence {
        var i = 0
        a[i]++
        while (a[i] >= k) {
            a[i] = 0
            if (++i >= n) return@generateSequence null
            a[i]++
        }
        a.toList()
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


fun Long.modinv(m: Long): Long {
    var a: Long = this
    var b: Long = m
    var aa: Long = 1
    var ba: Long = 0
    while (true) {
        val q: Long = a / b
        if (a == b * q) {
            if (b != 1L) { // Modular inverse does not exist!
                error("not exists")
            }
            while (ba < 0) ba += m
            return ba
        }
        val tmpA: Long = a
        val tmpAa: Long = aa
        a = b
        b = tmpA - b * q
        aa = ba
        ba = tmpAa - ba * q
    }
}
