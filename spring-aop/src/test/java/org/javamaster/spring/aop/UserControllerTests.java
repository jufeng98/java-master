package org.javamaster.spring.aop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yudong
 * @date 2021/4/26
 */
@AutoConfigureMockMvc
@SpringBootTest(classes = SpringAopApplication.class)
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void createUserNormalTests() throws Exception {
        mockMvc
                .perform(
                        post("/user/createUser")
                                .param("username", "jufeng98")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

}
