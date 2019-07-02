package org.javamaster.b2c.bytecode;

import org.javamaster.b2c.bytecode.utils.OMUtils;
import org.javamaster.b2c.bytecode.utils.ResourceUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

/**
 * 测试读取并解析class文件结构
 *
 * @author yudong
 * @date 2019/7/2
 */
public class ClassFileReaderTest {

    @Test
    public void test() throws Exception {
        File file = ResourceUtils
                .getFile("classpath:org/javamaster/b2c/bytecode/model/PersonCopy.class");
        ClassFileReader classFileReader = new ClassFileReader(new FileInputStream(file));
        System.out.println(OMUtils.writeValueAsString(classFileReader, true));
    }

}
