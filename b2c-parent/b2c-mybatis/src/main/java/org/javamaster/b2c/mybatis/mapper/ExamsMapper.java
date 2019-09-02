package org.javamaster.b2c.mybatis.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.mybatis.entity.Exams;
import org.javamaster.b2c.mybatis.entity.ExamsExample;

/**
 * 操纵考试表,请勿手工改动此文件,请使用 mybatis generator
 * 
 * @author mybatis generator
 */
public interface ExamsMapper {
    long countByExample(ExamsExample example);

    int deleteByExample(ExamsExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Exams record);

    int insertSelective(Exams record);

    List<Exams> selectByExample(ExamsExample example);

    Exams selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Exams record, @Param("example") ExamsExample example);

    int updateByExample(@Param("record") Exams record, @Param("example") ExamsExample example);

    int updateByPrimaryKeySelective(Exams record);

    int updateByPrimaryKey(Exams record);
}