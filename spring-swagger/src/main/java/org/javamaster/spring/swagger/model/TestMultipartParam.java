package org.javamaster.spring.swagger.model;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author yudong
 * @date 2022/4/18
 */
public class TestMultipartParam {
    private Integer age;
    private MultipartFile file;

    @Override
    public String toString() {
        return "TestMultipartParam{" +
                "age=" + age +
                ", file=" + file +
                '}';
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
