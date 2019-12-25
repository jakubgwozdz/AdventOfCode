package advent2019

import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.expect

fun <T> assertThat(t: T): AssertBuilder<T> = AssertBuilder(t)

class AssertBuilder<T>(val actual: T) {
    fun isTrue(op: (T) -> Boolean) =
        assertTrue { op.invoke(actual) }

    fun isFalse(op: (T) -> Boolean) =
        assertFalse { op.invoke(actual) }

    fun isTrue(message: String?, op: (T) -> Boolean) =
        assertTrue(message) { op.invoke(actual) }

    fun isFalse(message: String?, op: (T) -> Boolean) =
        assertFalse(message) { op.invoke(actual) }

}

fun <T> AssertBuilder<out Collection<T>>.hasElements(expected: Collection<T>) {
    if (actual == expected) return
    val eNotA = expected - actual
    val aNotE = actual - expected
    val message =
        """Collection has ${actual.size - aNotE.size} expected elements, but also ${aNotE.size} more, and not ${eNotA.size} expected:
        |  Actual: $aNotE
        |Expected: $eNotA""".trimMargin()
    throw AssertionError(message)

}

fun <T> expectSetOf(vararg v: T, op: () -> Iterable<T>) = expect(v.toSet()) { op().toSet() }