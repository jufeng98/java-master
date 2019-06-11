package org.javamaster.b2c.core.mapper;

import org.apache.ibatis.annotations.Param;
import org.javamaster.b2c.core.entity.MicrowebsiteExam;
import org.javamaster.b2c.core.enums.ExamTypeEnum;

import java.util.List;

/**
 * @author yudong
 * @date 2019/6/10
 */
public interface ManualMicrowebsiteMapper {
    List<MicrowebsiteExam> select(@Param("examType") ExamTypeEnum examType, @Param("username") String username);

}
