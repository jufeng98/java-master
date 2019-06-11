package org.javamaster.b2c.core.mapper;

import org.javamaster.b2c.core.entity.MicrowebsiteExam;

import java.util.List;

public interface MicrowebsiteExamMapper {
    int insert(MicrowebsiteExam record);


    List<MicrowebsiteExam> selectAll();

}