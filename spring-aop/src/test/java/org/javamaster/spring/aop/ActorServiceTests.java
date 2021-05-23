package org.javamaster.spring.aop;

import org.javamaster.spring.aop.service.ActorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SpringAopApplication.class)
class ActorServiceTests {

    @Autowired
    private ActorService actorService;

    @Test
    void createUserTest() {
        Integer id = actorService.createActor("jufeng98");
        Assertions.assertEquals(1, (int) id);
    }

}
