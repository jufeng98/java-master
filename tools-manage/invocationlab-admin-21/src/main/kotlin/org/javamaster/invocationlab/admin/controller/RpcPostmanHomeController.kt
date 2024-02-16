package org.javamaster.invocationlab.admin.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/**
 * 返回html文件
 *
 * @author yudong
 */
@Controller
class RpcPostmanHomeController {

    @RequestMapping(value = ["/"])
    fun index(): String {
        return "index.html"
    }

}
