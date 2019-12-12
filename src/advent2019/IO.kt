package advent2019

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalTime

fun logWithTime(msg: String, op: String.()->String = {this}) {
    println("${LocalTime.now().toString().substring(0,12).padEnd(12,'0')}: ${msg.op()}")
}

fun logWithTime(msg: List<Char>, op: String.()->String = {this}) {
    logWithTime(msg.joinToString(""), op)
}

fun readAllLines(fileName: String) = Files.readAllLines(Paths.get(fileName))
    .map { it.trim() }
