package advent2019.intcode

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.math.BigInteger

@Suppress("BlockingMethodInNonBlockingContext")
fun Flow<Char>.fullLines(): Flow<String> =
    flow {
        val builder = StringBuilder()
        collect {
            when (it) {
                '\n' -> emit(builder.toString()).also { builder.clear() }
                else -> builder.append(it)
            }
        }
    }

suspend fun SendChannel<BigInteger>.writeln(msg: String) {
    println(msg)
    msg.map { it.toInt().toBigInteger() }.forEach { send(it) }
    send('\n'.toInt().toBigInteger())
}