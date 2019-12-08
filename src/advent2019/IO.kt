package advent2019

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalTime

fun logWithTime(msg: String, op: String.()->String = {this}) {
    println("${LocalTime.now()}: ${msg.op()}")
}

fun logWithTime(msg: List<Char>, op: String.()->String = {this}) {
    logWithTime(msg.joinToString(""), op)
}

fun readFile(fileName: String) = Files.readAllLines(Paths.get(fileName))
    .map { it.trim() }
