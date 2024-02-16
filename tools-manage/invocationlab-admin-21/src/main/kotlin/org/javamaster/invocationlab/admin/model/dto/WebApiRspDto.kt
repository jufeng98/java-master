package org.javamaster.invocationlab.admin.model.dto

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * powered by WebApiResponse
 *
 * @param <T>
 * @author yudong
</T> */

@AllOpen
class WebApiRspDto<T> {
    var code = 0

    var error: String? = null

    var msg: String? = null

    var data: T? = null

    var elapse: Long = 0

    /**
     * 是否需要重试。只有在 code != 0，也就是说有错的时候才有意义。
     * 为真时表示一定要重试，为假时表示一定不要重试，为空时由调用方自行决定。
     */
    var needRetry: Boolean? = null

    companion object {
        private const val SUCCESS_CODE: Int = 0
        private const val ERROR_CODE: Int = 1
        fun <T> success(data: T): WebApiRspDto<T> {
            val response = WebApiRspDto<T>()
            response.code = SUCCESS_CODE
            response.data = data
            return response
        }

        fun <T> success(data: T, elapse: Long): WebApiRspDto<T> {
            val response = WebApiRspDto<T>()
            response.code = SUCCESS_CODE
            response.data = data
            response.elapse = elapse
            return response
        }

        @JvmStatic
        fun <T> error(errorMessage: String): WebApiRspDto<T> {
            return error(errorMessage, ERROR_CODE)
        }

        @JvmStatic
        fun <T> error(errorMessage: String, errorCode: Int): WebApiRspDto<T> {
            return error(errorMessage, errorCode, null)
        }

        fun <T> error(errorMessage: String, isNeedRetry: Boolean): WebApiRspDto<T> {
            return error(errorMessage, ERROR_CODE, isNeedRetry)
        }

        fun <T> error(errorMessage: String, errorCode: Int, isNeedRetry: Boolean?): WebApiRspDto<T> {
            val response = WebApiRspDto<T>()
            response.code = errorCode
            response.error = errorMessage
            response.needRetry = isNeedRetry
            return response
        }
    }
}
