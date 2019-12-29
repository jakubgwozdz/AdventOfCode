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
    val movements: LinkedList<Pair<String, Direction>> = LinkedList(),
    var currentRoomId: String? = null,
    var lastMovement: Direction? = null,
    val knownDirectionsToPlaces: MutableMap<String, List<Pair<String, Direction>>> = mutableMapOf(),
    var weightState: WeightState = WeightState.UNKNOWN,
    val inventory: MutableList<String> = mutableListOf()
)

class DecisionMaker(val state: SearchState) {

    private fun itemToTake(room: Room): String? =
        if (state.weightState == WeightState.TOO_HEAVY || state.weightState == WeightState.OK) null
        else room.items
            .firstOrNull { it !in forbiddenItems }

    private fun takeItem(room: Room, item: String): String {
        return "take $item"
    }

    // TODO - move $state modification somewhere else
    private fun makeMove(room: Room, directionToCheck: Direction?): String {
        val movement = directionToCheck
            ?.also { state.movements += room.name to it }
            ?: state.movements.removeLast()
                .second
                .back
        state.lastMovement = movement

        return movement.text
    }

    fun nextDirectionToCheck(room: Room): Direction? = (state.lastMovement ?: Direction.N)
        .let { listOf(it.left, it, it.right, it.back) }
        .filter { it in room.doors }
        .firstOrNull { it !in state.knownExits[room.name]?.keys ?: emptyList<Direction>() }

    fun makeDecision(): String {
        val room = state.knownRooms[state.currentRoomId!!]!!
        return itemToTake(room)
            ?.let { takeItem(room, it) }
            ?: makeMove(room, nextDirectionToCheck(room))
    }

}

class SearchUpdater(val state: SearchState) {

    fun update(output: Output) = when (output) {
        is RoomDescription -> room(output)
        is RoomWithTeleportDescription -> teleport(output)
        is TakeActionDescription -> itemTaken(output)
        is Prompt -> error("'Command?' not allowed here")
    }

    private fun itemTaken(output: TakeActionDescription) {
        val item = takeRegex.matchEntire(output.line)!!.groupValues[1]
        state.inventory += item
        state.knownRooms.compute(state.currentRoomId!!) { name, room -> room!!.copy(items = room.items - item) }
    }

    private fun teleport(description: RoomWithTeleportDescription) {
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
    }

    private fun room(description: RoomDescription) {
        val room = description.room
        updateMap(room)
    }

    private fun updateMap(room: Room) {
        val prevRoomId = state.currentRoomId

        val visitedAlready =
            state.knownRooms[room.name]?.also { if (it != room) error("this room was $it, now it's $room") }
        state.knownRooms[room.name] = room
        state.currentRoomId = room.name

        if (prevRoomId != null && state.lastMovement != null) {
            state.knownExits.computeIfAbsent(prevRoomId) { mutableMapOf() }[state.lastMovement!!] = room.name
        }
        state.knownDirectionsToPlaces.computeIfAbsent(room.name) { state.movements.toList() }
    }

}

@FlowPreview
class Cryostasis(val program: Memory) {

    val state = SearchState()
    val decisionMaker = DecisionMaker(state)
    val stateUpdater = SearchUpdater(state)

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
            .outputs()
            .collect {
                when (it) {
                    is Prompt -> inChannel.writeln(decisionMaker.makeDecision())
                    else -> stateUpdater.update(it)
                }
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
object Prompt : Output()
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
fun Flow<String>.outputs(): Flow<Output> =
    flow {
        val builder = OutputParser()
        collect { line ->
            when (line) {
                "Command?" -> {
                    builder.build().forEach { emit(it) }
                    builder.clear()
                    emit(Prompt)
                }
                else -> builder.accept(line)
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
        System.err.println("currentRoom: " + cryostasis.state.currentRoomId)
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
