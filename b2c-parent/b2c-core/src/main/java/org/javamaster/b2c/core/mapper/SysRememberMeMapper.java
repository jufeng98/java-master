package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.SysRememberMe;
import org.javamaster.b2c.core.entity.SysRememberMeExample;

import java.util.List;

/**
 * 操纵系统记住登录用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public interface SysRememberMeMapper {
    long countByExample(SysRememberMeExample example);

    int deleteByExample(SysRememberMeExample example);

    int deleteByPrimaryKey(String series);

    int insert(SysRememberMe record);

    int insertSelective(SysRememberMe record);

    List<SysRememberMe> selectByExample(SysRememberMeExample example);

    SysRememberMe selectByPrimaryKey(String series);

    int updateByExampleSelective(@Param("record") SysRememberMe record, @Param("example") SysRememberMeExample example);

    int updateByExample(@Param("record") SysRememberMe record, @Param("example") SysRememberMeExample example);

    int updateByPrimaryKeySelective(SysRememberMe record);

    int updateByPrimaryKey(SysRememberMe record);
}