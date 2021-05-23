package org.javamaster.spring.aop;

import org.javamaster.spring.aop.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringAopApplication.class)
class UserServicePointcutAdvisorTests {

    @Autowired
    private UserService userService;

    @Test
    void createUserTest() {
        Integer id = userService.createUser("jufeng98");
        Assertions.assertEquals(1, (int) id);
    }

}
