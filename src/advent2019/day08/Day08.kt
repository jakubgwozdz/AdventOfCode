package advent2019.day08

import advent2019.log
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val layers = Files.readString(Paths.get("input-2019-08.txt"))
        .trim()
        .chunked(25*6)
    val minLayer = layers.minBy { it.countChars('0') }!!
    val result = minLayer.countChars('1') * minLayer.countChars('2')
    log("part1 is $result")

    val merged = layers.reduce { a, b -> merge(a, b) }
    merged.chunked(25).forEach { log(it.replace('0',' ')) }
}

fun String.countChars(c: Char) = count { it == c }

fun merge(a: String, b:String):String {
    val chars = a.indices
        .map { if (a[it] == '2') b[it] else a[it] }
        .toCharArray()
    return String(chars)
}
