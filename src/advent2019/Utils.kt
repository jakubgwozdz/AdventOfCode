package advent2019

fun <T> oneOf(vararg v:T, predicate: (T)->Boolean):T = v.single(predicate)
fun <T> oneOfOrNull(vararg v:T, predicate: (T)->Boolean):T? = v.singleOrNull(predicate)

