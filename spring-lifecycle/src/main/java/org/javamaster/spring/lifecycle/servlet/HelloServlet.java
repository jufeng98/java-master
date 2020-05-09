package org.javamaster.spring.lifecycle.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

/**
 * @author yudong
 * @date 2020/5/9
 */
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 完整输出http请求内容
        System.out.println(req.getRequestURI() + "?" + req.getQueryString() + " " + req.getProtocol());
        Enumeration<String> enumeration = req.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement();
            Object headerValue = req.getHeader(headerName);
            System.out.println(headerName + ":" + headerValue);
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <meta charset=\"UTF-8\">");
        out.println("    <title>welcome</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + req.getParameter("username") + " welcome!</h1>");
        out.println("</body>");
        out.println("</html>");
    }

}
