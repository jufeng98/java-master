package org.javamaster.spring.tools.manage.inteceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;


/**
 * @author yudong
 */
public class AppInterceptor implements HandlerInterceptor {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String indexContent;

    static {
        try {
            String path = "/public/tools-manage-view/index.html";
            @Cleanup
            InputStream inputStream = AppInterceptor.class.getResourceAsStream(path);
            indexContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.info("{}:{}", request.getRemoteAddr(), requestURI);
        if (requestURI.equals("/tools-manage-view/")) {
            response.sendRedirect("/tools-manage-view/redisManage");
            return false;
        }
        if (requestURI.startsWith("/tools-manage-view")
                && !requestURI.endsWith("css")
                && !requestURI.endsWith("js")) {
            indexResponse(response);
            return false;
        }
        return true;
    }

    private void indexResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        @Cleanup
        PrintWriter writer = response.getWriter();
        writer.println(indexContent);
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
    }
}
