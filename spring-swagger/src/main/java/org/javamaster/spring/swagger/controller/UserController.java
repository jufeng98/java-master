package org.javamaster.spring.swagger.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.swagger.model.Result;
import org.javamaster.spring.swagger.model.User;
import org.javamaster.spring.swagger.model.UserReqVo;
import org.javamaster.spring.swagger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author yudong
 * @date 2022/1/4
 */
@Slf4j
@Api(tags = "用户相关")
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @ApiOperation("01-登录")
    @PostMapping("/login")
    public Result<User> login(@RequestBody @Validated UserReqVo userReqVo) {
        return Result.success(userService.login(userReqVo));
    }

    @ApiOperation("02-批量删除用户")
    @PostMapping("/batchDeleteUser")
    public Result<List<Integer>> batchDeleteUser(@RequestBody @Size(min = 1)
                                                 @ApiParam(required = true, allowMultiple = true,
                                                         examples = @Example(@ExampleProperty(mediaType = "userIdList", value = "[1,2]")))
                                                         List<Integer> userIdList) {
        return Result.success(userIdList);
    }
}
