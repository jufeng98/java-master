package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.SysGroupUser;

import java.util.List;

public interface SysGroupUserMapper {
    int insert(SysGroupUser record);

    List<SysGroupUser> selectAll();
}