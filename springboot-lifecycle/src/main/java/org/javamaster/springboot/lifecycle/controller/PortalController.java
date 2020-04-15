package org.javamaster.springboot.lifecycle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author yudong
 * @date 2020/4/14
 */
@Controller
@RequestMapping("/portal")
public class PortalController {

    @Autowired
    private ApplicationContext context;

    @GetMapping("/index")
    @ResponseBody
    public String index(HttpServletRequest request) {
        ApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();

        ServletContext servletContext = request.getSession().getServletContext();
        ApplicationContext applicationContext1 = (ApplicationContext) servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        System.out.println(applicationContext == applicationContext1);

        System.out.println(applicationContext == context.getParent());

        return "hello world";
    }

}
