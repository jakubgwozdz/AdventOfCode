package advent2019.day23

import advent2019.intcode.*
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import java.math.BigInteger

@FlowPreview
@ExperimentalCoroutinesApi
fun main() {

    val input = readAllLines("input-2019-23.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }
//        .also { disassemblyProgram(it).forEach { println(it) }}

    val program = parse(input)

    val nics = (0 until 50).map {
        NIC(it.toBigInteger(), program)
    }.associateBy { it.id }

    runBlocking {

        // init
        nics.values.forEach { nic ->
            launch {
                nic.inChannel.send(nic.id)

                while (true) {
                    println("waiting for ${nic.id}...")
                    val addr = nic.outChannel.receive()
                    println("${nic.id} sent addr $addr")
                    val x = nic.outChannel.receive()
                    println("${nic.id} sent x $x")
                    val y = nic.outChannel.receive()
                    println("${nic.id} sent y $y")
                    nics[addr]!!.inChannel.apply {
                        send(x)
                        send(y)
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

        }
        logWithTime("NICs initialization ready")

        // boot
        val jobs = nics.mapValues { (_, nic) ->
            launch {
                nic.comp.run()
            }
        }

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


@ExperimentalCoroutinesApi
class NIC(val id: BigInteger, program: Memory) {

    val inChannel = Channel<BigInteger>(Channel.UNLIMITED)
    val inBuffer = object : InBuffer {
        override suspend fun receive(): BigInteger {
            return if (inChannel.isEmpty) (-1).toBigInteger()
            else inChannel.receive()
        }
    }
    val outChannel = Channel<BigInteger>(Channel.UNLIMITED)
    val comp = Computer(id, program, inBuffer, ChannelOutBuffer(id, outChannel, true))

}