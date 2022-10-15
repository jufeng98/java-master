package org.javamaster.spring.swagger.feign;

import org.javamaster.spring.swagger.model.Result;
import org.javamaster.spring.swagger.model.User;
import org.javamaster.spring.swagger.model.UserReqVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 可配置 -Dfeign.debug.swagger.url=localhost:8964 用于本地开发调试
 *
 * @author yudong
 * @date 2022/10/15
 */
@FeignClient(value = "spring-swagger", path = "/user", url = "${feign.debug.swagger.url:}")
public interface UserFeignService {

    @PostMapping("/login")
    Result<User> login(@RequestBody UserReqVo userReqVo);

}
