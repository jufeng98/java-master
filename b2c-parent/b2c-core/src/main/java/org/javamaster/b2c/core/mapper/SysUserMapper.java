package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.SysUser;
import org.javamaster.b2c.core.entity.SysUserExample;

import java.util.List;

/**
 * 操纵系统用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 09:05:51
 */
public interface SysUserMapper {
    long countByExample(SysUserExample example);

    int deleteByExample(SysUserExample example);

    int deleteByPrimaryKey(String username);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    List<SysUser> selectByExample(SysUserExample example);

    SysUser selectByPrimaryKey(String username);

    int updateByExampleSelective(@Param("record") SysUser record, @Param("example") SysUserExample example);

    int updateByExample(@Param("record") SysUser record, @Param("example") SysUserExample example);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);
}