package org.javamaster.invocationlab.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 返回html文件
 *
 * @author yudong
 */
@Controller
public class RpcPostmanHomeController {

    @RequestMapping(value = "/")
    public String index() {
        return "index.html";
    }
}
