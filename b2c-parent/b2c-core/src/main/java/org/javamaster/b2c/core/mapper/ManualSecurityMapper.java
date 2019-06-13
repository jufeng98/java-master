package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.model.AuthUser;

/**
 * @author yudong
 * @date 2019/6/10
 */
public interface ManualSecurityMapper {

    AuthUser selectUser(@Param("username") String username);

    int deleteByUsername(@Param("username") String username);

}