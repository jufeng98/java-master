package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.SysRememberMe;

import java.util.List;

public interface SysRememberMeMapper {
    int deleteByPrimaryKey(String series);

    int deleteByUsername(String Username);

    int insert(SysRememberMe record);

    SysRememberMe selectByPrimaryKey(String series);

    List<SysRememberMe> selectAll();

    int updateByPrimaryKey(SysRememberMe record);
}