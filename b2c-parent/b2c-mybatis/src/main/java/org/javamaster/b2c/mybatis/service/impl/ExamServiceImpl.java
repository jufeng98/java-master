package org.javamaster.b2c.mybatis.service.impl;

import org.javamaster.b2c.mybatis.mapper.ExamsMapper;
import org.javamaster.b2c.mybatis.mapper.MenusMapper;
import org.javamaster.b2c.mybatis.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author yudong
 * @date 2022/1/24
 */
@Service
public class ExamServiceImpl implements ExamService {
    @Autowired
    private ExamsMapper examsMapper;

    @Autowired
    private MenusMapper menusMapper;

    @PostConstruct
    public void init() {
        System.out.println(examsMapper.getClass());
        System.out.println(menusMapper.getClass());
    }

}
