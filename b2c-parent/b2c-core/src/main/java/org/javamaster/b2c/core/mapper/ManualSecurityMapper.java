package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.model.AuthUser;

public interface ManualSecurityMapper {

    AuthUser selectUser(@Param("username") String username);

}