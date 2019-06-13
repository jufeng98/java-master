package org.javamaster.b2c.dubbo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.javamaster.b2c.dubbo.api.UserDubboService;
import org.javamaster.b2c.dubbo.dto.UserBaseDto;

import java.util.Date;

/**
 * @author yudong
 * @date 2019/6/13
 */
@Service(version = "1.0.0")
public class UserDubboServiceImpl implements UserDubboService {
    @Override
    public UserBaseDto getByUsername(String username) {
        // 模拟数据
        UserBaseDto userBaseDto = new UserBaseDto();
        userBaseDto.setUsername(username);
        userBaseDto.setMobile("13800138000");
        userBaseDto.setRegisterTime(new Date());
        return userBaseDto;
    }
}
