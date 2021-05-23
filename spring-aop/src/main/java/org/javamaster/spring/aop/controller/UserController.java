package org.javamaster.spring.aop.controller;

import org.javamaster.spring.aop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yudong
 * @date 2021/4/26
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 注入的是代理对象
     */
    @Autowired
    private UserService userService;

    @PostMapping("/createUser")
    public Integer createUser(String username) {
        return userService.createUser(username);
    }

}
