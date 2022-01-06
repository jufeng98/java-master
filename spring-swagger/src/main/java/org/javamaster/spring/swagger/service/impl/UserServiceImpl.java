package org.javamaster.spring.swagger.service.impl;

import org.javamaster.spring.swagger.enums.SexEnum;
import org.javamaster.spring.swagger.model.User;
import org.javamaster.spring.swagger.model.UserReqVo;
import org.javamaster.spring.swagger.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author yudong
 * @date 2022/1/4
 */
@Service
public class UserServiceImpl implements UserService {

    @Override
    public User login(UserReqVo userReqVo) {
        return user(userReqVo);
    }

    private User user(UserReqVo userReqVo) {
        User user = new User();
        user.setUserId("100000001");
        user.setUsername(userReqVo.getUsername());
        user.setDesc("超级管理员");
        user.setId(10001L);
        user.setBirthday(new Date());
        user.setSex(SexEnum.MAN.code);
        return user;
    }

}
