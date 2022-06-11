package org.javamaster.spring.embed.arthas.controller;

import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.embed.arthas.model.CreateUserReqVo;
import org.javamaster.spring.embed.arthas.model.Result;
import org.javamaster.spring.embed.arthas.model.SysUser;
import org.javamaster.spring.embed.arthas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yudong
 * @date 2022/6/4
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/createUser")
    public Result<SysUser> createUser(@Validated @RequestBody CreateUserReqVo reqVo) {
        return Result.success(userService.createUser(reqVo));
    }

}
