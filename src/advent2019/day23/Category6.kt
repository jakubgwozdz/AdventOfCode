package advent2019.day23

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

@FlowPreview
@ExperimentalCoroutinesApi
fun main() {

    val input = readAllLines("data/input-2019-23.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }
    Category6(input, logging = true).puzzlePart1()
        .also { logWithTime("part 1: $it") }
    Category6(input, logging = true).puzzlePart2()
        .also { logWithTime("part 2: $it") }

}

@FlowPreview
@ExperimentalCoroutinesApi
class Category6(val input: String, val size: Int = 50, val logging: Boolean = false) {

    val program = parseIntcode(input)

    fun puzzlePart1(): Long {

        val nics = (0 until 50)
            .map { id ->
                NIC(id.toLong(), program.copy())
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
                                ?: (if (addr == 255L) channel255 else error("unknown NIC $addr")))
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

    fun puzzlePart2(): Long {

        val nat = Channel<Packet>()
        val resultChannel = Channel<Packet>()

        val nics = (0 until 50)
            .map { id ->
                NIC(id.toLong(), program.copy())
            }
            .associateBy { it.id }

        return runBlocking {

            // init
            val outputJobs = nics.values.shuffled().map { nic ->
                nic.id to launch {
                    nic.outChannel.consumeAsFlow()
                        .asPackets()
                        .collect {
                            if (logging) println("${nic.id} sends $it")
                            val addr = it.first
                            val packet = it.second
                            val outChannel = (nics[addr]?.inChannel
                                ?: (if (addr == 255L) nat else error("unknown NIC $addr")))
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

            val natChannel = launch {
                nat.consumeEach { packet ->
                    lastNatPacketChannel.send(packet)
                        .also { if (logging) logWithTime("NAT received $packet...") }
                }
            }

            val natJob = launch {
                var lastNatPacketSent: Packet? = null
                while (true) {
                    delay(300)
                    if (nics.values.all { it.isIdle }) {
                        val lastNatPacketReceived = lastNatPacketChannel.receive()
                        if (logging) logWithTime("NAT sends $lastNatPacketReceived...")
                        nics[0L]!!.inChannel.send(lastNatPacketReceived)
                        if (lastNatPacketReceived == lastNatPacketSent) {
                            if (logging) logWithTime("...it's same as previously")
                            resultChannel.send(lastNatPacketSent)
                        }
                        lastNatPacketSent = lastNatPacketReceived
                    }
                }
            }

            val result = resultChannel.receive()
            this.coroutineContext.cancelChildren()
            if (logging) logWithTime("All jobs cancelled, hopefully")
            joinAll()

            result
        }
            .second
    }
}

typealias Packet = Pair<Long, Long>
typealias AddressedPacket = Pair<Long, Packet>

fun Flow<Long>.asPackets(): Flow<AddressedPacket> = flow {
    val packet = mutableListOf<Long>()
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
    val id: Long,
    program: Memory,
    val stateChangedChannel: Channel<Pair<Long, Boolean>> = Channel(Channel.CONFLATED)
) {

    var isIdle = false
    private val idleValue = listOf(-1L)

    private val idleAnswerOp: suspend () -> List<Long> = {
        idleValue
            .also { isIdle = true }
    }

    private val translateOp: suspend (Packet) -> List<Long> = {
        listOf(it.first, it.second)
            .also { isIdle = false }
    }

    val inChannel = Channel<Packet>(Channel.UNLIMITED)
    val inBuffer = TranslatingNonblockingInBuffer(
        id,
        inChannel,
        idleAnswerOp = idleAnswerOp,
        translateOp = translateOp
    )
        .apply { buffer.add(this@NIC.id) }

    val outChannel = Channel<Long>()
    val comp = Intcode(program, inBuffer, ChannelOutBuffer(id, outChannel), id)

}