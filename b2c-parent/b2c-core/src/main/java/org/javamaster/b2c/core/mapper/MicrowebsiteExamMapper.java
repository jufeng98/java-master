package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.MicrowebsiteExam;
import org.javamaster.b2c.core.entity.example.MicrowebsiteExamExample;

import java.util.List;

/**
 * 操纵考试表
 * 
 * @author mybatis generator
 * @date 2019/06/13 09:05:51
 */
public interface MicrowebsiteExamMapper {
    long countByExample(MicrowebsiteExamExample example);

    int deleteByExample(MicrowebsiteExamExample example);

    int insert(MicrowebsiteExam record);

    int insertSelective(MicrowebsiteExam record);

    List<MicrowebsiteExam> selectByExample(MicrowebsiteExamExample example);

    int updateByExampleSelective(@Param("record") MicrowebsiteExam record, @Param("example") MicrowebsiteExamExample example);

    int updateByExample(@Param("record") MicrowebsiteExam record, @Param("example") MicrowebsiteExamExample example);
}