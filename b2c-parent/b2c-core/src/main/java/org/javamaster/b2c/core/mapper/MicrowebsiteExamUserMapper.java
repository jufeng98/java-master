package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.MicrowebsiteExamUser;
import org.javamaster.b2c.core.entity.example.MicrowebsiteExamUserExample;

import java.util.List;

/**
 * 操纵考试关联用户表
 * 
 * @author mybatis generator
 * @date 2019/06/13 08:38:38
 */
public interface MicrowebsiteExamUserMapper {
    long countByExample(MicrowebsiteExamUserExample example);

    int deleteByExample(MicrowebsiteExamUserExample example);

    int insert(MicrowebsiteExamUser record);

    int insertSelective(MicrowebsiteExamUser record);

    List<MicrowebsiteExamUser> selectByExample(MicrowebsiteExamUserExample example);

    int updateByExampleSelective(@Param("record") MicrowebsiteExamUser record, @Param("example") MicrowebsiteExamUserExample example);

    int updateByExample(@Param("record") MicrowebsiteExamUser record, @Param("example") MicrowebsiteExamUserExample example);
}