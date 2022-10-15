package org.javamaster.spring.swagger.controller;

import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.swagger.feign.UserFeignService;
import org.javamaster.spring.swagger.model.Result;
import org.javamaster.spring.swagger.model.User;
import org.javamaster.spring.swagger.model.UserReqVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yudong
 * @date 2022/10/15
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestFeignController {

    @Autowired
    private UserFeignService userFeignService;

    @GetMapping("/feignHotswap")
    public String feignHotswap() {
        UserReqVo userReqVo = new UserReqVo();
        userReqVo.setUsername("jufeng98");
        userReqVo.setPassword("123456");
        Result<User> result = userFeignService.login(userReqVo);
        log.info("res:{}", result);
        return "success";
    }

}
