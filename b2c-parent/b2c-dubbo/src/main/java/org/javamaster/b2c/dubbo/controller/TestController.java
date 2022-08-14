package org.javamaster.b2c.dubbo.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import org.javamaster.b2c.dubbo.api.UserDubboService;
import org.javamaster.b2c.dubbo.dto.UserBaseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: yudong
 */
@RestController
@RequestMapping("/appTest")
public class TestController {
    Logger log = LoggerFactory.getLogger(getClass());
    @Reference(version = "1.0.0", check = false, timeout = 6000)
    private UserDubboService userDubboService;

    @GetMapping("/testDubboHotswap")
    public UserBaseDto testDubboHotswap() {
        UserBaseDto baseDto = userDubboService.getByUsername("jufeng98");
        log.info("baseDto:{}", baseDto);
        return baseDto;
    }

}
