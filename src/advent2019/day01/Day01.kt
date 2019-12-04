package advent2019.day01

import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val sum = Files.readAllLines(Paths.get("input-2019-01.txt"))
        .asSequence()
        .map { it.toInt() }
//        .map { it to fuel(it) } // first star
        .map { it to fuel2(it) } // second star
        .onEach { println(it) }
        .map { it.second }
        .sum()

    println(sum)

}

fun fuel(mass: Int): Int {
    return mass / 3 - 2
}

fun fuel2(mass: Int): Int {
    var r = 0
    var f = fuel(mass)
    while (f > 0) {
        r += f
        f = fuel(f)
    }
    return r
}

