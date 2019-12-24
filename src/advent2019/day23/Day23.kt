package advent2019.day23

import advent2019.bi
import advent2019.intcode.*
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.math.BigInteger
import kotlin.coroutines.ContinuationInterceptor

@FlowPreview
@ExperimentalCoroutinesApi
fun main() {

    val input = readAllLines("data/input-2019-23.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }
//        .also { disassemblyProgram(it).forEach { println(it) } }

    val program = parse(input)

    val nics = (0 until 50)
//        .shuffled()
        .map {
            NIC(it.toBigInteger(), program.copy())
        }
        .associateBy { it.id }

    runBlocking {

        val resultChannel = Channel<Packet>()

        // init
        val outputJobs = nics.values.shuffled().map { nic ->
            nic.id to launch {
                nic.outChannel.consumeAsFlow()
                    .asPackets()
                    .collect {
                        println("${nic.id} sends $it")
                        val addr = it.first
                        val packet = it.second
                        (nics[addr]?.inChannel
                            ?: (if (addr == 255.bi) resultChannel else error("unknown NIC $addr"))
                                ).send(packet)
                    }
//                }
            }
        }.toMap()

        logWithTime("NICs initialization ready")

        // boot
        val jobs = nics.values.map { nic ->
            nic.id to launch {
                nic.comp.run()
            }
        }.toMap()

        logWithTime("NICs started")

        val result = resultChannel.receive()
        this.coroutineContext.cancelChildren()

        result
    }.also { logWithTime("part 1: ${it.second}") }

}

typealias Packet = Pair<BigInteger, BigInteger>
typealias AddressedPacket = Pair<BigInteger, Packet>

fun Flow<BigInteger>.asPackets(): Flow<AddressedPacket> =

    flow {
        val packet = mutableListOf<BigInteger>()
        collect {
            packet += it
            if (packet.size == 3) {
                emit(packet[0] to (packet[1] to packet[2]))
                packet.clear()
            }
        }
    }

suspend fun nodelay() {
//    if (timeMillis <= 0) return // don't delay
    return suspendCancellableCoroutine { cont: CancellableContinuation<Unit> ->
        (cont.context[ContinuationInterceptor]!! as Delay).scheduleResumeAfterDelay(0, cont)
    }
}

@ExperimentalCoroutinesApi
class NIC(val id: BigInteger, program: Memory) {

    val inChannel = Channel<Packet>(Channel.UNLIMITED)
    val inBuffer = NonblockingInBuffer()

    val outChannel = Channel<BigInteger>()
    val comp = Computer(id, program, inBuffer, ChannelOutBuffer(id, outChannel))

    fun memCpy() = comp.memory.copy().map

    var lastMemory = program.copy().map
    var lastIp = 0.bi

    inner class NonblockingInBuffer : InBuffer {
        var lastEmpty = false
        var nextRequired = true
        var buffer: BigInteger? = id

        override suspend fun receive(): BigInteger {
            return buffer
//                ?.also { println("$id received $it") }
                ?.also { buffer = null }
                ?: if (inChannel.isEmpty) {
                    nodelay() // suspend here for a moment so other threads may work - TODO make in look nicer
                    (-1).bi
                } else {
                    val packet = inChannel.receive()
                        .also { println("                          $id received $it") }
                    buffer = packet.second
                    packet.first
//                        .also { println("$id received $it") }
                }
        }

    }

}