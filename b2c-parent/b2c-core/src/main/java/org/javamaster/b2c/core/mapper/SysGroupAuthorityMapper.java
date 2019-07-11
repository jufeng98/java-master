package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.SysGroupAuthority;
import org.javamaster.b2c.core.entity.SysGroupAuthorityExample;

import java.util.List;

/**
 * 操纵系统组表关联权限表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public interface SysGroupAuthorityMapper {
    long countByExample(SysGroupAuthorityExample example);

    int deleteByExample(SysGroupAuthorityExample example);

    int insert(SysGroupAuthority record);

    int insertSelective(SysGroupAuthority record);

    List<SysGroupAuthority> selectByExample(SysGroupAuthorityExample example);

    int updateByExampleSelective(@Param("record") SysGroupAuthority record, @Param("example") SysGroupAuthorityExample example);

    int updateByExample(@Param("record") SysGroupAuthority record, @Param("example") SysGroupAuthorityExample example);
}