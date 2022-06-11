package org.javamaster.spring.embed.arthas.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.javamaster.spring.embed.arthas.model.CreateUserReqVo;
import org.javamaster.spring.embed.arthas.model.SysUser;
import org.javamaster.spring.embed.arthas.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author yudong
 * @date 2022/6/4
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Override
    public SysUser createUser(CreateUserReqVo reqVo) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(reqVo, sysUser);
        String md5Pwd = DigestUtils.md5DigestAsHex(reqVo.getPassword().getBytes(StandardCharsets.UTF_8));
        sysUser.setPassword(md5Pwd);
        return sysUser;
    }

}
