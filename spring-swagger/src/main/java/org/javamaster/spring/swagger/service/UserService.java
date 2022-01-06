package org.javamaster.spring.swagger.service;


import org.javamaster.spring.swagger.model.User;
import org.javamaster.spring.swagger.model.UserReqVo;

/**
 * @author yudong
 * @date 2022/1/4
 */
public interface UserService {
    User login(UserReqVo userReqVo);
}
