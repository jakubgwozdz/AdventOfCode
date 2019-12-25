package advent2019.day23

import advent2019.bi
import advent2019.logWithTime
import advent2019.readAllLines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.expect

private val input = readAllLines("data/input-2019-23.txt").single()

@ExperimentalCoroutinesApi
@FlowPreview
internal class Category6KtTest {

    @Test
    fun puzzlePart1() {
        expect(27061.bi) { Category6(input).puzzlePart1() }
    }

    @Test
    fun puzzlePart2() {
        expect(19406.bi) { Category6(input).puzzlePart2() }
    }
}
