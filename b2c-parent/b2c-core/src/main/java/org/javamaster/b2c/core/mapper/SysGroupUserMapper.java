package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.SysGroupUser;
import org.javamaster.b2c.core.entity.example.SysGroupUserExample;

import java.util.List;

/**
 * 操纵系统组表关联用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public interface SysGroupUserMapper {
    long countByExample(SysGroupUserExample example);

    int deleteByExample(SysGroupUserExample example);

    int insert(SysGroupUser record);

    int insertSelective(SysGroupUser record);

    List<SysGroupUser> selectByExample(SysGroupUserExample example);

    int updateByExampleSelective(@Param("record") SysGroupUser record, @Param("example") SysGroupUserExample example);

    int updateByExample(@Param("record") SysGroupUser record, @Param("example") SysGroupUserExample example);
}