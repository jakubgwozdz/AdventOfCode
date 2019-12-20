package advent2019

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalTime

fun<T> logWithTime(msg: T, op: T.()->String = {"$this"}) {
    println("${LocalTime.now().toString().take(12).padEnd(12)}: ${msg.op()}")
}

fun logWithTime(msg: List<Char>, op: String.()->String = {this}) {
    logWithTime(msg.joinToString(""), op)
}

fun readAllLines(fileName: String) = Files.readAllLines(Paths.get(fileName))
    .map { it }
