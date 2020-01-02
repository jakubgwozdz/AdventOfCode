package advent2019.day16

import advent2019.logWithTime
import advent2019.readAllLines

fun main() {
    val input = readAllLines("data/input-2019-16.txt").single()
        .also { logWithTime("input length: ${it.length}") }

    fft(input, 100, 0, 8)
        .also { logWithTime("part 1: $it") }

    fftRepeat(input, 10000, 100, input.take(7).toInt(), 8)
        .also { logWithTime("part 2: $it") }

}

fun fftSingleDigit(input: IntArray, i: Int): Int {
    var sum = 0
    (i + 1..input.size).forEach {
        if ((it / (i + 1)) % 4 == 1) sum += input[it - 1]
        if ((it / (i + 1)) % 4 == 3) sum -= input[it - 1]
    }

    return when {
        sum > 0 -> sum % 10
        sum < 0 -> (-sum) % 10
        else -> 0
    }
}

fun fftPhase(input: IntArray): IntArray {
    return IntArray(input.size) { fftSingleDigit(input, it) }
}

fun fft(input: String, iterations: Int, offset: Int = 0, messageLen: Int = input.length): String {
    val fft = (1..iterations).fold(input.toDigits()) { provider, iter ->
        fftPhase(provider)
    }
    return fft.asString(offset, messageLen)
}

fun fftRepeat(input: String, repeats: Int, iterations: Int, offset: Int, messageLen: Int): String {
    if (offset < input.length * repeats / 2) error("shortcut won't work")
    var secondHalf = (offset until input.length * repeats).map { it to (input[it % input.length] - '0') }.toMap()
    repeat(iterations) {
        var a = 0
        secondHalf = (input.length * repeats - 1 downTo offset).map { i ->
            i to (a + secondHalf[i]!!)
                .also { a = it }
                .let { if (it < 0) -it else it } % 10
        }.toMap()
    }
    return (offset until offset + messageLen).map { secondHalf[it]!! }.joinToString("")
}


internal fun String.toDigits() = IntArray(length) { this[it] - '0' }
internal fun IntArray.asString(offset: Int = 0, messageLen: Int = this.size) =
    buildString { (offset until offset + messageLen).forEach { append('0' + this@asString[it]) } }
