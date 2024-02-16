package org.javamaster.invocationlab.admin.redis

fun interface TripleFunction<T, R, V, U> {
    fun apply(t: T, r: R, v: V): U
}
