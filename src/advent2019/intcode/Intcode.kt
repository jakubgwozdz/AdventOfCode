package advent2019.intcode


import advent2019.logWithTime
import advent2019.remove
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.util.*

class Memory(initial: Map<Long, Long>) {
    val map = initial.toMutableMap()
    operator fun get(addr: Long) = map[addr] ?: 0L
    operator fun set(addr: Long, value: Long) {
        map[addr] = value
    }

    val size: Long get() = map.keys.max() ?: 0L

    fun copy() = Memory(map.toMutableMap())
}

fun opcode(operation: Long) = operation % 100

// params handling
enum class ParamMode { Position, Immediate, Relative }

fun nthParamMode(n: Int, operation: Long): ParamMode {
    var mode = operation / 10
    repeat(n) { mode /= 10 }
    return when (mode % 10) {
        0L -> ParamMode.Position
        1L -> ParamMode.Immediate
        2L -> ParamMode.Relative
        else -> error("Unknown mode in opcode $operation")
    }
}

interface InBuffer<T> {
    suspend fun receive(): T
}

interface OutBuffer {
    suspend fun send(v: Long)
    fun close(): Boolean
}

class ChannelInBuffer<T>(val id: Any, val channel: ReceiveChannel<T>, val logIO: Boolean = false) :
    InBuffer<T> {
    override suspend fun receive(): T {
        if (logIO) print("$id <-- ...")
        return channel.receive().also { if (logIO) println("\b\b\b$it") }
    }
}

@ExperimentalCoroutinesApi
class TranslatingNonblockingInBuffer<T : Any, P : Any, I : Any>(
    val id: I,
    val channel: ReceiveChannel<P>,
    val logIO: Boolean = false,
    val idleAnswerOp: suspend () -> List<T>,
    val translateOp: suspend (P) -> List<T>
) : InBuffer<T> {

    val buffer: MutableList<T> = LinkedList() // LinkedList better?

    override suspend fun receive(): T {
        delay(100)
        return buffer.remove()
            ?: if (channel.isEmpty) {
//                nodelay() // suspend here for a moment so other threads may work - TODO make in look nicer
                buffer += idleAnswerOp()
                buffer.remove()!!
            } else {
                val packet = channel.receive()
                    .also { if (logIO) println("                          $id received $it") }
                buffer += translateOp(packet)
                buffer.remove()!!
            }
    }
}


class ChannelOutBuffer(val id: Any, val channel: SendChannel<Long>, val logIO: Boolean = false) : OutBuffer {
    override suspend fun send(v: Long) {
        channel.also { if (logIO) print("$id --> $v...") }.send(v).also { if (logIO) println("\b\b\b done") }
    }

    override fun close() = channel.close()

}

var compId = 0L

class Intcode(
    val memory: Memory,
    val inBuffer: InBuffer<Long>,
    val outBuffer: OutBuffer,
    val id: Any = compId++,
    val debug: Boolean = false
) {
    constructor(
        memory: Memory,
        receiveChannel: ReceiveChannel<Long>,
        sendChannel: SendChannel<Long>,
        id: Any = compId++,
        debug: Boolean = false
    ) : this(memory, ChannelInBuffer(id, receiveChannel), ChannelOutBuffer(id, sendChannel), id, debug)

    var ip: Long = 0 // instruction pointer
    var rb: Long = 0 // relative base

    suspend fun read(): Long {
        val job = GlobalScope.launch {
            delay(1000)
            logWithTime("Computer $id waits for input> ")
        }
        return try {
            inBuffer.receive()
        } finally {
            job.cancel()
        }
    }

    suspend fun write(v: Long) {
        val job = GlobalScope.launch {
            delay(1000)
            logWithTime("Computer $id wants to write> ")
        }
        try {
            outBuffer.send(v)
        } finally {
            job.cancel()
        }
    }

    val operation: Long get() = memory[ip]

    private fun nthAddr(n: Int): Long =
        when (nthParamMode(n, operation)) {
            ParamMode.Position -> memory[ip + n]
            ParamMode.Immediate -> ip + n
            ParamMode.Relative -> rb + memory[ip + n]
        }

    private val firstAddr get() = nthAddr(1)
    private val secondAddr get() = nthAddr(2)
    private val thirdAddr get() = nthAddr(3)

    // main loop
    suspend fun run() {
        while (true) {
            if (debug)
                logWithTime("${ip.toString().padStart(6)}: ${dissassembly(memory, ip)}")
            when (opcode(operation)) {
                1L -> opADD()
                2L -> opMUL()
                3L -> opIN()
                4L -> opOUT()
                5L -> opJNZ()
                6L -> opJZ()
                7L -> opSETL()
                8L -> opSETE()
                9L -> opMOVRB()
                99L -> {
//                    outBuffer.close()
                    return
                }
                else -> error("unknown opcode $operation at addr $ip")
            }
            yield()
        }
    }

    // operations

    private fun opADD() {
        memory[thirdAddr] = memory[firstAddr] + memory[secondAddr]
        ip += 4
    }

    private fun opMUL() {
        memory[thirdAddr] = memory[firstAddr] * memory[secondAddr]
        ip += 4
    }

    private suspend fun opIN() {
        memory[firstAddr] = read()
        ip += 2
    }

    private suspend fun opOUT() {
        write(memory[firstAddr])
        ip += 2
    }

    private fun opJNZ() {
        ip = when {
            memory[firstAddr] != 0L -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun opJZ() {
        ip = when {
            memory[firstAddr] == 0L -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun opSETL() {
        memory[thirdAddr] = if (memory[firstAddr] < memory[secondAddr]) 1L else 0L
        ip += 4
    }

    private fun opSETE() {
        memory[thirdAddr] = if (memory[firstAddr] == memory[secondAddr]) 1L else 0L
        ip += 4
    }

    private fun opMOVRB() {
        rb += memory[firstAddr]
        ip += 2
    }

}

fun parseIntcode(input: String): Memory {
    return input
        .split(",")
        .mapIndexed { index: Int, s: String -> index.toLong() to s.trim().toLong() }
        .toMap()
        .let { Memory(it) }
}

//operator fun Long.plus(n: Int): Long = this + n.toLong()
//
//operator fun Long.div(i: Int): Long = this / i.toLong()
//
//operator fun Long.rem(i: Int): Int = (this % i.toLong()).toInt()
