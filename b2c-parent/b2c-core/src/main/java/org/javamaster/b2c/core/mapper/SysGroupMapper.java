package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.SysGroup;

import java.util.List;

public interface SysGroupMapper {
    int deleteByPrimaryKey(String groupCode);

    int insert(SysGroup record);

    SysGroup selectByPrimaryKey(String groupCode);

    List<SysGroup> selectAll();

    int updateByPrimaryKey(SysGroup record);
}