package advent2019.day23

import advent2019.bi
import advent2019.intcode.*
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.math.BigInteger
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@FlowPreview
@ExperimentalCoroutinesApi
fun main() {

    val input = readAllLines("data/input-2019-23.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }
        .also { disassemblyProgram(it).forEach { println(it) }}

    val program = parse(input)

    val nics = (0 until 50).map {
        NIC(it.toBigInteger(), program)
    }.associateBy { it.id }

    runBlocking {

        // init
        val outputJobs = nics.values.map { nic ->
            nic.id to launch {
                nic.inChannel.send(nic.id)

                while (true) {
                    logWithTime("waiting for ${nic.id}...")
                    val addr = nic.outChannel.receive()
                    logWithTime("${nic.id} sent addr $addr")
                    val x = nic.outChannel.receive()
                    logWithTime("${nic.id} sent x $x")
                    val y = nic.outChannel.receive()
                    logWithTime("${nic.id} sent y $y")
                    nics[addr]!!.inChannel.apply {
                        send(x)
                        send(y)
                        logWithTime("sent $x $y to $addr")
                    }
//                nic.outChannel.consumeAsFlow()
//                    .asPackets()
//                    .onEach {
//                        println(it)
//                        nics[it.first]!!.inChannel.apply {
//                            send(it.second)
//                            send(it.third)
//                        }
//                    }
//                    .collect { println("${nic.id} got $it") }
                }
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

        joinAll()

    }

}


fun Flow<BigInteger>.asPackets(): Flow<Triple<BigInteger, BigInteger, BigInteger>> =

    flow {
        val packet = mutableListOf<BigInteger>()
        collect {
            packet += it
            if (packet.size == 3) {
                emit(Triple(packet[0], packet[1], packet[2]))
                packet.clear()
            }
        }
    }

/** Returns [Delay] implementation of the given context */
internal val CoroutineContext.delay: Delay get() = get(ContinuationInterceptor)!! as Delay

suspend fun nodelay() {
//    if (timeMillis <= 0) return // don't delay
    return suspendCancellableCoroutine sc@ { cont: CancellableContinuation<Unit> ->
        cont.context.delay.scheduleResumeAfterDelay(0, cont)
    }
}

@ExperimentalCoroutinesApi
class NIC(val id: BigInteger, program: Memory) {

    val inChannel = Channel<BigInteger>(Channel.UNLIMITED)
    val inBuffer = NonblockingInBuffer(program)

    val outChannel = Channel<BigInteger>()
    val comp = Computer(id, program, inBuffer, ChannelOutBuffer(id, outChannel))

    fun memCpy() = comp.memory.copy().map

    inner class NonblockingInBuffer(initial: Memory):InBuffer {
        var lastEmpty = false
        var nextRequired = true

        var lastMemory = initial.map
        var lastIp = 0.bi

        override suspend fun receive(): BigInteger = if (!nextRequired && inChannel.isEmpty) {
            if (!lastEmpty) {
                val currMemory = memCpy()
                val currIp = comp.ip
                println("      $id's memory changed? ${currMemory != lastMemory} ; oldPos: $lastIp ; newPos: $currIp")
                lastIp = currIp
                lastMemory = currMemory
//                println("         $id's input empty")
            }
            lastEmpty = true
            nodelay()
            suspendCoroutine<BigInteger> { it.resume((-1).bi) }
        } else {
            nextRequired = !nextRequired
            lastEmpty = false
//            print("         $id <-- ...")
            inChannel.receive()
//                .also { println("\b\b\b$it") }
        }

    }

}