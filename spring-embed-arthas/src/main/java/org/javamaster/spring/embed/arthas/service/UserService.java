package org.javamaster.spring.embed.arthas.service;

import org.javamaster.spring.embed.arthas.model.CreateUserReqVo;
import org.javamaster.spring.embed.arthas.model.SysUser;

/**
 * @author yudong
 * @date 2022/6/4
 */
public interface UserService {

    SysUser createUser(CreateUserReqVo reqVo);

}
