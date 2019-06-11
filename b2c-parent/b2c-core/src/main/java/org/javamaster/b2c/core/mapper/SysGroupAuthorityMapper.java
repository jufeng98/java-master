package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.SysGroupAuthority;

import java.util.List;

public interface SysGroupAuthorityMapper {
    int insert(SysGroupAuthority record);

    List<SysGroupAuthority> selectAll();
}