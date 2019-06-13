package org.javamaster.b2c.dubbo.api;

import org.javamaster.b2c.dubbo.dto.UserBaseDto;

/**
 * @author yudong
 * @date 2019/6/13
 */
public interface UserDubboService {
    UserBaseDto getByUsername(String username);
}
