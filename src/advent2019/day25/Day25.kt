package advent2019.day25

import advent2019.intcode.Memory
import advent2019.intcode.parseIntcode
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.FlowPreview

class Cryostasis(val program: Memory) {

}

@FlowPreview
fun main() {
    val input = readAllLines("data/input-2019-25.txt").single()
        .also { logWithTime("Program length (chars): ${it.length}") }

    Cryostasis(parseIntcode(input))
//    goSpring(input, spring1())
//        .also { logWithTime("part 1: $it") }

//    goSpring(input, spring2())
//        .also { logWithTime("part 2: $it") }

}
