package org.javamaster.spring.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamaster.spring.test.boot.ScanDependenciesContextBootstrapper;
import org.javamaster.spring.test.config.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.javamaster.spring.test.GeneralTestCode.PROFILE_UNIT_TEST;

/**
 * 公共测试基类
 *
 * @author yudong
 * @date 2021/5/13
 */
@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        PropertyTestConfig.class,
        WebMvcTestConfig.class
}, initializers = VerifyEnvApplicationContextInitializer.class)
@AutoConfigureMockMvc
@AutoConfigureWebMvc
@WebAppConfiguration
@BootstrapWith(ScanDependenciesContextBootstrapper.class)
@ActiveProfiles(PROFILE_UNIT_TEST)
public abstract class GeneralTestCode {
    public static final String PROFILE_UNIT_TEST = "unit-test";
    @Autowired
    protected WebApplicationContext context;
    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();

        customizedObjectMapper(objectMapper);

        DefaultMockMvcBuilder mockMvcBuilder = MockMvcBuilders.webAppContextSetup(context);

        customizedMockMvc(mockMvcBuilder);

        mockMvc = mockMvcBuilder.build();
    }

    public void customizedObjectMapper(ObjectMapper objectMapper) {
    }

    public void customizedMockMvc(DefaultMockMvcBuilder mockMvcBuilder) {
    }
}