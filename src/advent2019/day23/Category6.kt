package advent2019.day23

import advent2019.bi
import advent2019.intcode.*
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import java.math.BigInteger

@FlowPreview
@ExperimentalCoroutinesApi
fun main() {

    val input = readAllLines("data/input-2019-23.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }
    Category6(input).puzzlePart1()
        .also { logWithTime("part 1: $it") }
    Category6(input).puzzlePart2()
        .also { logWithTime("part 2: $it") }

}

@FlowPreview
@ExperimentalCoroutinesApi
class Category6(val input: String, val size: Int = 50, val logging: Boolean = false) {

    val program = parse(input)

    fun puzzlePart1(): BigInteger {

        val nics = (0 until 50)
            .map { id ->
                NIC(id.toBigInteger(), program.copy())
            }
            .associateBy { it.id }

        return runBlocking {

            val channel255 = Channel<Packet>()

            // init
            val outputJobs = nics.values.shuffled().map { nic ->
                nic.id to launch(Dispatchers.Default) {
                    nic.outChannel.consumeAsFlow()
                        .asPackets()
                        .collect {
                            if (logging) println("${nic.id} sends $it")
                            val addr = it.first
                            val packet = it.second
                            val outChannel = (nics[addr]?.inChannel
                                ?: (if (addr == 255.bi) channel255 else error("unknown NIC $addr")))
                            outChannel.send(packet)
                        }
                }
            }.toMap()

            if (logging) logWithTime("NICs initialization ready")

            // boot
            val jobs = nics.values.map { nic ->
                nic.id to launch(Dispatchers.Default) {
                    nic.comp.run()
                }
            }.toMap()

            if (logging) logWithTime("NICs started")

            val result = channel255.receive()
            this.coroutineContext.cancelChildren()
            if (logging) logWithTime("All jobs cancelled, hopefully")

            result
        }
            .second
    }

    fun puzzlePart2(): BigInteger {

        val nat = Channel<Packet>()
        val resultChannel = Channel<Packet>()

        val stateChangedChannel = Channel<Pair<BigInteger, Boolean>>()

        val nics = (0 until 50)
            .map { id ->
                NIC(id.toBigInteger(), program.copy())
            }
            .associateBy { it.id }

        return runBlocking {

            // init
            val outputJobs = nics.values.shuffled().map { nic ->
                nic.id to launch() {
                    nic.outChannel.consumeAsFlow()
                        .asPackets()
                        .collect {
                            if (logging) println("${nic.id} sends $it")
                            val addr = it.first
                            val packet = it.second
                            val outChannel = (nics[addr]?.inChannel
                                ?: (if (addr == 255.bi) nat else error("unknown NIC $addr")))
                            outChannel.send(packet)
                        }
                }
            }.toMap()

            if (logging) logWithTime("NICs initialization ready")

            // boot
            val jobs = nics.values.map { nic ->
                nic.id to launch(Dispatchers.Default) {
                    nic.comp.run()
                }
            }.toMap()

            if (logging) logWithTime("NICs started")

            val lastNatPacketChannel = Channel<Packet>(Channel.CONFLATED)
            var lastNatPacketSent: Packet? = null

            val natChannel = launch() {
                nat.consumeEach { packet ->
                    lastNatPacketChannel.send(packet)
                        .also { logWithTime("NAT received $packet...") }
                }
            }

            val natJob = launch() {
                while (true) {
                    delay(300)
                    if (nics.values.all { it.isIdle }) {
                        val lastNatPacketReceived = lastNatPacketChannel.receive()
                        logWithTime("NAT sends $lastNatPacketReceived...")
                        nics[0.bi]!!.inChannel.send(lastNatPacketReceived)
                        if (lastNatPacketReceived == lastNatPacketSent) {
                            logWithTime("...it's same as previously")
                            resultChannel.send(lastNatPacketSent!!)
                        }
                        lastNatPacketSent = lastNatPacketReceived
                    }
                }
            }

            val idles = nics.mapValues { false }.toMutableMap()
            val stateChangedJob = launch() {
                stateChangedChannel.consumeEach { (id, idle) ->
                    idles[id] = idle
//                    if (idle) {
//                        if (idles.all { it.value }) {
//                            val lastNatPacketReceived = lastNatPacketChannel.receive()
//                            logWithTime("NAT sends $lastNatPacketReceived...")
//                            nics[0.bi]!!.inChannel.send(lastNatPacketReceived)
//                            if (lastNatPacketReceived == lastNatPacketSent) {
//                                logWithTime("...it's same as previously")
//                                resultChannel.send(lastNatPacketSent!!)
//                            }
//                            lastNatPacketSent = lastNatPacketReceived
//                        }
//                    }
                }
            }

            val result = resultChannel.receive()
            this.coroutineContext.cancelChildren()
            if (logging) logWithTime("All jobs cancelled, hopefully")

            result
        }
            .second
    }
}

typealias Packet = Pair<BigInteger, BigInteger>
typealias AddressedPacket = Pair<BigInteger, Packet>

fun Flow<BigInteger>.asPackets(): Flow<AddressedPacket> = flow {
    val packet = mutableListOf<BigInteger>()
    collect {
        packet += it
        if (packet.size == 3) {
            emit(packet[0] to (packet[1] to packet[2]))
            packet.clear()
        }
    }
}

@ExperimentalCoroutinesApi
class NIC(
    val id: BigInteger,
    program: Memory,
    val stateChangedChannel: Channel<Pair<BigInteger, Boolean>> = Channel(Channel.CONFLATED)
) {

    var isIdle = false
    private val idleValue = listOf((-1).bi)

    private val idleAnswerOp: suspend () -> List<BigInteger> = {
        idleValue
            .also {
                isIdle = true
                stateChangedChannel.send(id to isIdle)
            }
    }

    private val translateOp: suspend (Packet) -> List<BigInteger> = {
        listOf(it.first, it.second)
            .also {
                isIdle = false
                stateChangedChannel.send(id to isIdle)
            }
    }

    val inChannel = Channel<Packet>(Channel.UNLIMITED)
    val inBuffer = TranslatingNonblockingInBuffer(
        id,
        inChannel,
        idleAnswerOp = idleAnswerOp,
        translateOp = translateOp
    )
        .apply { buffer.add(this@NIC.id) }

    val outChannel = Channel<BigInteger>()
    val comp = Computer(id, program, inBuffer, ChannelOutBuffer(id, outChannel))

}