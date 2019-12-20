package advent2019

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.expect

fun <T> assertThat(t: T): AssertBuilder<T> = AssertBuilder(t)

class AssertBuilder<T>(val t: T) {
    fun isTrue(op: (T) -> Boolean) =
        assertTrue { op.invoke(t) }

    fun isFalse(op: (T) -> Boolean) =
        assertFalse { op.invoke(t) }

    fun isTrue(message: String?, op: (T) -> Boolean) =
        assertTrue(message) { op.invoke(t) }

    fun isFalse(message: String?, op: (T) -> Boolean) =
        assertFalse(message) { op.invoke(t) }

}

fun <T> expectSetOf(vararg v:T, op:()->Iterable<T>) = expect(v.toSet()) { op().toSet() }