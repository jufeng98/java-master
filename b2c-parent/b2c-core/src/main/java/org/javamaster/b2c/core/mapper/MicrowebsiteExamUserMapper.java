package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.MicrowebsiteExam;
import org.javamaster.b2c.core.entity.MicrowebsiteExamUser;
import org.javamaster.b2c.core.enums.ExamTypeEnum;

import java.util.List;

public interface MicrowebsiteExamUserMapper {
    int insert(MicrowebsiteExamUser record);

    List<MicrowebsiteExamUser> selectAll();

    List<MicrowebsiteExam> select(@Param("examType") ExamTypeEnum examType, @Param("username") String username);

}