package org.javamaster.spring.swagger.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.swagger.model.User;
import org.javamaster.spring.swagger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yudong
 * @date 2022/12/21
 */
@Slf4j
@RestController
@RequestMapping("/lambda")
public class LambdaController {
    @Autowired
    private UserService userService;
    private final List<User> userList = Lists.newArrayList(
            User.builder().username("hello").build(),
            User.builder().username("world").build()
    );

    @PostMapping("/test")
    public void test() {
        userList
                // Lambda表达式
                .forEach(user -> {
                    System.out.println(user.getUsername());
                    System.out.println(user.getClass());
                    System.out.println(userService.login(null));
                });
    }
}
