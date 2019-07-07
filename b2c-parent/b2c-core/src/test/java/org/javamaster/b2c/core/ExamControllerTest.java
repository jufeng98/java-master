package org.javamaster.b2c.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * @author yudong
 * @date 2019/6/10
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CoreApplication.class)
@WebAppConfiguration
public class ExamControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mockMvc;

    protected static MockHttpSession session = new MockHttpSession();

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "1050106266",
            password = "123456",
            authorities = "ROLE_ADMIN")
    public void testGetExamList() throws Exception {
        Map<String, Object> reqVo = new HashMap<>();
        reqVo.put("examType", 1);
        System.out.println(objectMapper.writeValueAsString(reqVo));
        mockMvc.perform(MockMvcRequestBuilders.post("/json/exam/getExamList")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqVo))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "1050106266",
            password = "123456",
            authorities = "ROLE_ADMIN")
    public void testGetExamListByOpInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/json/exam/getExamListByOpInfo")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("examOpDate", "2019-06-10")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

