package org.javamaster.invocationlab.admin.service

import java.util.*

/**
 * @author yudong
 */
class Pair<L, R>(val left: L, val right: R) {
    override fun toString(): String {
        return StringJoiner(", ", Pair::class.java.simpleName + "[", "]")
            .add("left=$left")
            .add("right=$right")
            .toString()
    }

    companion object {
        fun <L, R> of(left: L, right: R): Pair<L, R> {
            return Pair(left, right)
        }
    }
}
