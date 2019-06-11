package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.SysAuthority;

import java.util.List;

public interface SysAuthorityMapper {
    int deleteByPrimaryKey(String authorityCode);

    int insert(SysAuthority record);

    SysAuthority selectByPrimaryKey(String authorityCode);

    List<SysAuthority> selectAll();

    int updateByPrimaryKey(SysAuthority record);
}