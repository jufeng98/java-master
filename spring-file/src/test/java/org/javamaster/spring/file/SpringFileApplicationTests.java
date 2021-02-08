package org.javamaster.spring.file;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yudong
 * @date 2021/2/8
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpringFileApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void uploadFileTest() {
        File file = ResourceUtils.getFile("classpath:loading.gif");
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                MediaType.IMAGE_GIF_VALUE,
                new FileInputStream(file)
        );
        ResultActions resultActions = mockMvc.perform(multipart("/upload/uploadFile")
                .file(mockMultipartFile).accept(MediaType.APPLICATION_JSON));
        resultActions.andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .setCharacterEncoding(StandardCharsets.UTF_8.name());
        resultActions.andDo(print());
    }

}
