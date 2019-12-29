package advent2019.day25

import advent2019.intcode.parseIntcode
import advent2019.readAllLines
import kotlinx.coroutines.FlowPreview
import org.junit.jupiter.api.Test
import kotlin.test.expect

private val input = readAllLines("data/input-2019-25.txt").single()

@FlowPreview
internal class Day25KtTest {
    @Test
    fun puzzlePart1() {
        expect("84410376") { Cryostasis(parseIntcode(input)).start() }
    }
}