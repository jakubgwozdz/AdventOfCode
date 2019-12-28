package advent2019.day25

import advent2019.intcode.*
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigInteger
import java.util.*

@FlowPreview
class Cryostasis(val program: Memory) {

    fun start() = runBlocking {
        val inChannel = Channel<BigInteger>()
        val outChannel = Channel<BigInteger>()
        val computer = Intcode(program, inChannel, outChannel)

        launch {
            computer.run()
            inChannel.close()
            outChannel.close()
        }

        outChannel.consumeAsFlow()
            .map { it.toInt().toChar() }
            .fullLines()
            .onEach { println(it) }
            .infos()
            .collect {
                val command = update(it)
                inChannel.writeln(command)
            }
    }

    val map = SearchTree<Direction, Room>()
    val knownRooms = mutableMapOf<String, SearchTree<Direction, Room>>()
    val movements = LinkedList<Pair<Direction, String>>()
    var currentRoom: SearchTree<Direction, Room>? = null
//    val roomEnters = mutableMapOf<String, Direction>()

    fun update(output: Output): String {

        val prevRoom = currentRoom
        logWithTime("Previous room: ${prevRoom?.root?.name}, movements so far: $movements")

        return when (output) {
            is RoomDescription -> {
                val room = output.room
                val visitedAlready = knownRooms[room.name]
                if (prevRoom == null) {
                    currentRoom = map.start(room).also { knownRooms[room.name] = it }
                } else {
                    currentRoom = if (visitedAlready == null) {
                        val newRoom = prevRoom.add(movements.last().first, room)
                        knownRooms[room.name] = newRoom
                        newRoom
                    } else {
                        // we were here
                        prevRoom.update(movements.last.first, visitedAlready)
                    }
                }
                val movement = room.doors.firstOrNull { it !in currentRoom!!.visitedExits.keys }
                    ?.also { movements += it to room.name }
                    ?: movements.removeLast()
                        .also { if (it.second != prevRoom!!.root!!.name) error("last movement $it should be from ${prevRoom.root!!.name}") }
                        .first
                        .back

                movement.text
            }
            is RoomWithTeleportDescription -> TODO()
        }

    }
}

enum class Direction(val text: String) {

    N("north"), E("east"), S("south"), W("west");

    val back: Direction
        get() = when (this) {
            N -> S
            E -> W
            S -> N
            W -> E
        }

    val left: Direction
        get() = when (this) {
            N -> W
            E -> N
            S -> E
            W -> S
        }

    val right: Direction
        get() = when (this) {
            N -> E
            E -> S
            S -> W
            W -> N
        }

    companion object {
        fun from(s: String): Direction = values().single { it.text == s }
    }
}

class SearchTree<D : Any, T : Any>() {

    constructor(root: T) : this() {
        start(root)
    }

    var root: T? = null
        private set

    val visitedExits = mutableMapOf<D, SearchTree<D, T>>()

    fun start(root: T): SearchTree<D, T> {
        this.root = root
        return this
    }

    fun add(direction: D, location: T): SearchTree<D, T> {
        val newNode = SearchTree<D, T>(location)
        visitedExits[direction] = newNode
        return newNode
    }

    fun update(direction: D, node: SearchTree<D, T>): SearchTree<D, T> {
        visitedExits[direction] = node
        return node
    }
}

sealed class Output
data class RoomDescription(val room: Room) : Output()
data class RoomWithTeleportDescription(val room: Room) : Output()

class OutputParser() {

    enum class State { START, BUILDING_ROOM }

    var state = State.START
    val roomBuilder = RoomBuilder()
    val builtOutputs = mutableListOf<Output>()

    fun clear() {
        roomBuilder.clear()
        builtOutputs.clear()
        state = State.START
    }

    val alertRegex =
        Regex("A loud, robotic voice says \"Alert! Droids on this ship are (heavier) than the detected value!\" and you are ejected back to the checkpoint.")

    fun accept(line: String) {
        state = when (state) {
            State.START -> when {
                line.isBlank() -> state
                roomNameRegex.matches(line) -> {
                    roomBuilder.accept(line)
                    State.BUILDING_ROOM
                }
                else -> TODO()
            }
            State.BUILDING_ROOM -> {
                if (alertRegex.matches(line)) {
                    builtOutputs.add(RoomWithTeleportDescription(roomBuilder.build()))
                    clear()
                    state
                } else {
                    roomBuilder.accept(line)
                    state
                }
            }
        }
    }

    fun build(): List<Output> = when (state) {
        State.BUILDING_ROOM -> {
            builtOutputs.add(RoomDescription(roomBuilder.build()))
            builtOutputs.toList().also { clear() }
        }
        else -> error("Unexpected build() in state $state")

    }

}


@Suppress("BlockingMethodInNonBlockingContext")
fun Flow<String>.infos(): Flow<Output> =
    flow {
        val builder = OutputParser()
        collect {
            when (it) {
                "Command?" -> builder.build().forEach { emit(it) }.also { builder.clear() }
                else -> builder.accept(it)
            }
        }
    }


@FlowPreview
fun main() {
    val input = readAllLines("data/input-2019-25.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    Cryostasis(parseIntcode(input)).start()
//    goSpring(input, spring1())
//        .also { logWithTime("part 1: $it") }

//    goSpring(input, spring2())
//        .also { logWithTime("part 2: $it") }

}
