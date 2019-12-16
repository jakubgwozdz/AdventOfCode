package advent2019.day16

fun fftPhase(input: String): String {
    TODO()
}

fun fftPattern(i: Int, l: Int): List<Int> {
    val p = listOf(0, 1, 0, -1)
    var t = 0
    return generateSequence { p[t++ % 4] }
        .flatMap { f -> sequence { repeat(i) { yield(f) } } }
        .drop(1)
        .take(l)
        .toList()
}

