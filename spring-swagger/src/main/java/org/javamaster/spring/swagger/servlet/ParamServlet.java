package org.javamaster.spring.swagger.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javamaster.spring.swagger.enums.Sex;
import org.javamaster.spring.swagger.enums.SexEnum;
import org.javamaster.spring.swagger.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author yudong
 * @date 2022/4/23
 */
@WebServlet(urlPatterns = "/servlet/param/test")
public class ParamServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer age = Integer.valueOf(req.getParameter("age"));

        int age1 = Integer.parseInt(req.getParameter("age1"));

        String[] strAges = req.getParameterValues("ages");
        Integer[] ages = new Integer[strAges.length];
        int[] ages1 = new int[strAges.length];
        for (int i = 0; i < strAges.length; i++) {
            ages[i] = Integer.valueOf(strAges[i]);
            ages1[i] = Integer.parseInt(strAges[i]);
        }

        Sex sex = Sex.valueOf(req.getParameter("sex"));

        SexEnum sexEnum = SexEnum.getByCode(req.getParameter("sexEnum"));

        logger.info("req:{} {} {} {} {} {}", age, age1, Arrays.toString(ages), Arrays.toString(ages1), sex, sexEnum);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter writer = resp.getWriter();
        writer.print(objectMapper.writeValueAsString(Result.success("")));
    }

}
