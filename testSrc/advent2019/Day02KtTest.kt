package advent2019

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class Day02KtTest {

    @Test
    fun testParse() {
        val input = "1,9,10,3,2,3,11,0,99,30,40,50\n"
        val program = parse(input)
        assertThat(program.asList()).containsExactly(1, 9, 10, 3, 2, 3, 11, 0, 99, 30, 40, 50)
    }

    @Test
    fun samples() {
        assertAll(
            { assertThat(parse("1,9,10,3,2,3,11,0,99,30,40,50").also{ process(it)}[0]).isEqualTo(3500) },
            { assertThat(parse("1,0,0,0,99").also { process(it) }[0]).isEqualTo(2) },
            { assertThat(parse("2,3,0,3,99").also { process(it) }[0]).isEqualTo(2) },
            { assertThat(parse("1,1,1,4,99,5,6,0,99").also { process(it) }[0]).isEqualTo(30) }
        )
    }

}
