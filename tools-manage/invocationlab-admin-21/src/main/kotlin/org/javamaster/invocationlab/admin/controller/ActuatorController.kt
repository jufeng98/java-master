package org.javamaster.invocationlab.admin.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

/**
 * @author yudong
 */
@RestController
@RequestMapping("/actuator")
class ActuatorController {

    @ResponseBody
    @GetMapping("/health")
    fun health(): String {
        return "{\"status\":\"UP\"}"
    }

}
