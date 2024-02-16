package org.javamaster.invocationlab.admin.model

import org.javamaster.invocationlab.admin.annos.AllOpen

/**
 * @author yudong
 * @date 2023/2/14
 */

@AllOpen
class ResultVo<T>(var code: Int? = null, var msg: String? = null, val data: T? = null) {

    companion object {
        fun <T> success(data: T): ResultVo<T> {
            return ResultVo(200, "操作成功", data)
        }

        fun <T> fail(msg: String?): ResultVo<T> {
            return ResultVo(400, msg, null)
        }

        fun <T> fail(code: Int?, msg: String?): ResultVo<T> {
            return ResultVo(code, msg, null)
        }
    }

}
