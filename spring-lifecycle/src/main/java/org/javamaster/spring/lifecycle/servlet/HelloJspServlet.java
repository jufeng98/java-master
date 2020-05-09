package org.javamaster.spring.lifecycle.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yudong
 * @date 2020/5/9
 */
public class HelloJspServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        // 在Servlet里完成各类复杂的业务逻辑处理
        // ......
        // 将数据传递给jsp页面,由jsp页面进行展示
        // 这种设计就是所谓的MVC设计模式(此处springMVC应有掌声)
        req.setAttribute("username", req.getParameter("username"));
        req.getRequestDispatcher("/WEB-INF/views/welcome.jsp").forward(req, resp);
    }

}
