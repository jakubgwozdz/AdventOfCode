package advent2019

import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun <T> assertThat(t: T): AssertBuilder<T> = AssertBuilder(t)

class AssertBuilder<T>(val t: T) {
    fun isTrue(op: (T) -> Boolean) =
        assertTrue { op.invoke(t) }

    fun isFalse(op: (T) -> Boolean) =
        assertFalse { op.invoke(t) }

    fun isTrue(message: kotlin.String?, op: (T) -> Boolean) =
        assertTrue(message) { op.invoke(t) }

    fun isFalse(message: kotlin.String?, op: (T) -> Boolean) =
        assertFalse(message) { op.invoke(t) }

}