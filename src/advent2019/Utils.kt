package advent2019

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.ContinuationInterceptor

fun <T> oneOf(vararg v: T, predicate: (T) -> Boolean): T = v.single(predicate)
fun <T> oneOfOrNull(vararg v: T, predicate: (T) -> Boolean): T? = v.singleOrNull(predicate)

val Int.bi get() = toBigInteger()
val Long.bi get() = toBigInteger()


suspend fun nodelay() {
//    if (timeMillis <= 0) return // don't delay
    return suspendCancellableCoroutine { cont: CancellableContinuation<Unit> ->
        (cont.context[ContinuationInterceptor]!! as Delay).scheduleResumeAfterDelay(0, cont)
    }
}

fun <T : Any> MutableList<T>.remove(): T? = if (isEmpty()) null else removeAt(0)