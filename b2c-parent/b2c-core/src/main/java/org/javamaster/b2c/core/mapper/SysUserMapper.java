package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.SysUser;

import java.util.List;

public interface SysUserMapper {
    int deleteByPrimaryKey(String username);

    int insert(SysUser record);

    SysUser selectByPrimaryKey(String username);

    List<SysUser> selectAll();

    int updateByPrimaryKey(SysUser record);
}