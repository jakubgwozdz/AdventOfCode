package advent2019.day23

import advent2019.bi
import advent2019.readAllLines
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.test.expect

@ExperimentalCoroutinesApi
@FlowPreview
internal class Category6KtTest {

    @Test
    @Timeout(1)
    internal fun puzzlePart1() {
        expect(27061.bi) { Category6(readAllLines("data/input-2019-23.txt").single()).puzzlePart1() }
    }

    @Test
    @Timeout(1)
    internal fun puzzlePart2() {
        expect(19406.bi) { Category6(readAllLines("data/input-2019-23.txt").single()).puzzlePart2() }
    }
}
