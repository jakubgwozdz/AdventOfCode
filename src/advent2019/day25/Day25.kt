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

val forbiddenItems = listOf("escape pod", "giant electromagnet", "photons", "infinite loop", "molten lava")

enum class WeightState { UNKNOWN, TOO_HEAVY, TOO_LIGHT, OK }

data class SearchState(
    val knownRooms: MutableMap<String, Room> = mutableMapOf(),
    val knownExits: MutableMap<String, MutableMap<Direction, String>> = mutableMapOf(),
    val movements: LinkedList<Pair<String, Direction>> = LinkedList(), var currentRoom: Room? = null,
    var lastMovement: Direction? = null,
    val knownDirectionsToPlaces: MutableMap<String, List<Pair<String, Direction>>> = mutableMapOf(),
    var weightState: WeightState = WeightState.UNKNOWN,
    val inventory: MutableList<String> = mutableListOf()
) {
}

@FlowPreview
class Cryostasis(val program: Memory) {

    val state = SearchState()

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
                if (command.isNotBlank())
                    inChannel.writeln(command)
            }
    }

    fun update(output: Output): String = when (output) {
        is RoomDescription -> room(output)
        is RoomWithTeleportDescription -> teleport(output)
        is TakeActionDescription -> itemTaken(output)
    }

    private fun itemTaken(output: TakeActionDescription): String {
        val item = takeRegex.matchEntire(output.line)!!.groupValues[1]
        state.inventory += item
        val room = state.currentRoom!!
        val newRoom = room.copy(items = room.items - item)
        state.knownRooms[room.name] = newRoom
        return itemToTake(newRoom)
            ?.let { takeItem(room, room.items.first()) }
            ?: makeMove(room)
    }

    private fun teleport(description: RoomWithTeleportDescription): String {
        val room = description.room
        updateMap(room)
        state.lastMovement = null
        state.movements.clear()
        state.movements.addAll(state.knownDirectionsToPlaces[room.name]!!)
        state.movements.removeLast()
        val cause = alertRegex.matchEntire(description.reason)!!.groupValues[1]
        state.weightState = when (cause) {
            "heavier" -> WeightState.TOO_LIGHT
            else -> TODO(cause)
        }
        return ""
    }

    private fun room(description: RoomDescription): String {
        val room = description.room
        updateMap(room)
        return itemToTake(room)
            ?.let { takeItem(room, room.items.first()) }
            ?: makeMove(room)
    }

    private fun itemToTake(room: Room): String? =
        if (state.weightState == WeightState.TOO_HEAVY || state.weightState == WeightState.OK) null
        else room.items
            .firstOrNull { it !in forbiddenItems }

    private fun takeItem(room: Room, item: String): String {
        return "take $item"
    }

    private fun makeMove(room: Room): String {
        val movement = nextDirectionToCheck(room)
            ?.also { state.movements += room.name to it }
            ?: state.movements.removeLast()
//                .also { logWithTime("No unknown exit, TURNING AROUND") }
                //                        .also { if (it.first != prevRoom!!.name) error("last movement $it should be from ${prevRoom.name}") }
                .second
                .back
        state.lastMovement = movement

        return movement.text
    }

    private fun updateMap(room: Room) {
        val prevRoom = state.currentRoom
//        logWithTime("Previous room: ${prevRoom?.name}, movements so far: ${state.movements}")

        val knownExitsFromRoom = room.doors.map { it to state.knownExits[room.name]?.get(it) }
//        logWithTime("Current room: ${room.name}, exits: $knownExitsFromRoom")

        val visitedAlready =
            state.knownRooms[room.name]?.also { if (it != room) error("this room was $it, now it's $room") }
        state.knownRooms[room.name] = room
        state.currentRoom = room

        if (prevRoom != null && state.lastMovement != null) {
            state.knownExits.computeIfAbsent(prevRoom.name) { mutableMapOf() }[state.lastMovement!!] = room.name
        }
        state.knownDirectionsToPlaces.computeIfAbsent(room.name) { state.movements.toList() }
    }

    fun nextDirectionToCheck(room: Room): Direction? = (state.lastMovement ?: Direction.N)
        .let { listOf(it.left, it, it.right, it.back) }
        .filter { it in room.doors }
//        .filter { it != Direction.W || room.name != "Security Checkpoint" }
        .firstOrNull { it !in state.knownExits[room.name]?.keys ?: emptyList<Direction>() }
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
data class TakeActionDescription(val line: String) : Output()
data class RoomWithTeleportDescription(val room: Room, val reason: String) : Output()

val alertRegex =
    Regex("A loud, robotic voice says \"Alert! Droids on this ship are (heavier) than the detected value!\" and you are ejected back to the checkpoint.")


class OutputParser() {

    enum class State { START, BUILDING_ROOM, AFTER_TELEPORT, AFTER_TAKE }

    var state = State.START
    val roomBuilder = RoomBuilder()
    val builtOutputs = mutableListOf<Output>()

    fun clear() {
        roomBuilder.clear()
        builtOutputs.clear()
        state = State.START
    }

    fun accept(line: String) {
        state = when (state) {
            State.START, State.AFTER_TELEPORT, State.AFTER_TAKE -> when {
                line.isBlank() -> state
                roomNameRegex.matches(line) -> {
                    roomBuilder.accept(line)
                    State.BUILDING_ROOM
                }
                takeRegex.matches(line) -> {
                    builtOutputs.add(TakeActionDescription(line))
                    State.AFTER_TAKE
                }
                else -> TODO("$state, $line")
            }
            State.BUILDING_ROOM -> {
                if (alertRegex.matches(line)) {
                    builtOutputs.add(RoomWithTeleportDescription(roomBuilder.build(), line))
                    roomBuilder.clear()
                    State.AFTER_TELEPORT
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
        State.AFTER_TAKE -> {
            builtOutputs.toList().also { clear() }
        }
        else -> error("Unexpected 'Command?' in state $state")

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

    val cryostasis = Cryostasis(parseIntcode(input))
    try {
        cryostasis.start()
    } catch (e: Throwable) {
        System.err.println(e)
        System.err.println("movements: " + cryostasis.state.movements)
        System.err.println("lastMovement: " + cryostasis.state.lastMovement)
        System.err.println("currentRoom: " + cryostasis.state.currentRoom)
        System.err.println("knownRooms: " + cryostasis.state.knownRooms)
        System.err.println("knownExits: " + cryostasis.state.knownExits)
        e.stackTrace
            .map { it.toString() }
            .filter { it.startsWith("advent") }
            .forEach { System.err.println("  at $it") }
    }
//    goSpring(input, spring1())
//        .also { logWithTime("part 1: $it") }

//    goSpring(input, spring2())
//        .also { logWithTime("part 2: $it") }

}
