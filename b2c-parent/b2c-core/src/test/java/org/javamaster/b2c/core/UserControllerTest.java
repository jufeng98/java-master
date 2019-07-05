package org.javamaster.b2c.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamaster.b2c.core.entity.SysUser;
import org.javamaster.b2c.core.model.Page;
import org.javamaster.b2c.core.model.vo.ChangeUserStatusReqVo;
import org.javamaster.b2c.core.model.vo.CreateUserReqVo;
import org.javamaster.b2c.core.model.vo.FindUsersReqVo;
import org.javamaster.b2c.core.model.vo.UpdatePasswordReqVo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author yudong
 * @date 2019/7/5
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CoreApplication.class)
@WebAppConfiguration
public class UserControllerTest {

    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

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
    public void testCreateUser() throws Exception {
        CreateUserReqVo reqVo = new CreateUserReqVo();
        reqVo.setUsername("1050106888");
        reqVo.setPassword("123456");
        System.out.println(objectMapper.writeValueAsString(reqVo));
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/user/createUser")
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
    public void testChangeUserStatus() throws Exception {
        ChangeUserStatusReqVo reqVo = new ChangeUserStatusReqVo();
        reqVo.setUsername("1050106158");
        reqVo.setEnabled(false);
        System.out.println(objectMapper.writeValueAsString(reqVo));
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/user/changeUserStatus")
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
    public void testFindUsers() throws Exception {
        FindUsersReqVo reqVo = new FindUsersReqVo();
        SysUser user = new SysUser();
        user.setEnabled(true);
        reqVo.setSysUser(user);
        Page page = new Page();
        page.setPageNum(0);
        page.setPageSize(10);
        reqVo.setPage(page);
        System.out.println(objectMapper.writeValueAsString(reqVo));
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/user/findUsers")
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
    public void testUpdatePassword() throws Exception {
        UpdatePasswordReqVo reqVo = new UpdatePasswordReqVo();
        reqVo.setUsername("1050106158");
        reqVo.setNewPassword("654321");
        System.out.println(objectMapper.writeValueAsString(reqVo));
        mockMvc.perform(MockMvcRequestBuilders.post("/admin/user/updatePassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reqVo))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}

