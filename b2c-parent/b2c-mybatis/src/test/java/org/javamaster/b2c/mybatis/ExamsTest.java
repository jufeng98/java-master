package org.javamaster.b2c.mybatis;

import org.javamaster.b2c.mybatis.entity.Exams;
import org.javamaster.b2c.mybatis.entity.ExamsExample;
import org.javamaster.b2c.mybatis.mapper.ExamsMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author yudong
 * @date 2019/9/1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MybatisApplication.class)
public class ExamsTest {

    @Autowired
    ExamsMapper examsMapper;

    @Test
    public void testInsert() {
        Exams exams = new Exams();
        exams.setExamsCode("E00001");
        exams.setExamsName("Java基础考试");
        exams.setExamsDesc("Java基础考试,很重要");
        exams.setCreateUsername("admin");
        System.out.println(examsMapper.insertSelective(exams));
        System.out.println(exams.getId());
    }

    @Test
    public void testFind() {
        ExamsExample examsExample = new ExamsExample();
        examsExample.createCriteria().andExamsCodeEqualTo("E00001");
        System.out.println(examsMapper.selectByExample(examsExample));
    }
}
