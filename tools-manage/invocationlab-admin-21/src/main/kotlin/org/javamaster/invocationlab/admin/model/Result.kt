package org.javamaster.invocationlab.admin.model

import org.javamaster.invocationlab.admin.annos.AllOpen
import org.apache.commons.lang3.tuple.Pair
import java.io.Serial
import java.io.Serializable


@AllOpen
class Result<T> : Serializable {
    var isSuccess: Boolean? = null
    var responseCode: Int? = null
    var responseMsg: String? = null
    var data: T? = null
    var count: Long? = null

    constructor()

    constructor(data: T) : this(true, 0, "请求成功", data, null)

    constructor(pair: Pair<T, Long?>) : this(true, 0, "请求成功", pair.left, pair.right)

    constructor(data: T, count: Long?) : this(true, 0, "请求成功", data, count)

    constructor(responseCode: Int?, responseMsg: String?) : this(false, responseCode, responseMsg, null, null)

    @JvmOverloads
    constructor(isSuccess: Boolean?, responseCode: Int?, responseMsg: String?, data: T? = null, count: Long? = null) {
        this.isSuccess = isSuccess
        this.responseCode = responseCode
        this.responseMsg = responseMsg
        this.data = data
        this.count = count
    }

    companion object {
        @Serial
        private val serialVersionUID = -490238572284858235L
        fun <T> success(data: T): Result<T> {
            return Result(data)
        }

        fun <T> success(data: T, count: Long): Result<T> {
            return Result(data, count)
        }

        fun <T> fail(responseCode: Int?, responseMsg: String?): Result<T> {
            return Result(responseCode, responseMsg)
        }
    }
}
