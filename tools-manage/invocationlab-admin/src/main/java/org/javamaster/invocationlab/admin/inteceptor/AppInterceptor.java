package org.javamaster.invocationlab.admin.inteceptor;

import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.util.CookieUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.javamaster.invocationlab.admin.consts.ErdConst.COOKIE_TOKEN;


/**
 * @author yudong
 */
public class AppInterceptor extends HandlerInterceptorAdapter implements HandlerInterceptor {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String indexContent;

    static {
        try {
            String path = "/public/invocationlab-erd-online-view/index.html";
            @Cleanup
            InputStream inputStream = AppInterceptor.class.getResourceAsStream(path);
            indexContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final String[] paths = new String[]{
            "/invocationlab-erd-online-view/index.html",
            "/invocationlab-erd-online-view/login",
            "/invocationlab-erd-online-view/project",
            "/invocationlab-erd-online-view/design"
    };

    @SuppressWarnings("NullableProblems")
    @SneakyThrows
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.info("{}:{}", request.getRemoteAddr(), requestURI);
        for (String path : paths) {
            if (requestURI.startsWith(path)) {
                // 支持react的BrowserRouter
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                @Cleanup
                PrintWriter writer = response.getWriter();
                writer.println(indexContent);
                return false;
            }
        }
        if (requestURI.equals("/")) {
            response.sendRedirect("/invocationlab-rpcpostman-view/index.html");
            return false;
        }
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) SpringUtils.getContext()
                .getBean("redisTemplateJackson");
        TokenVo tokenVo = (TokenVo) redisTemplate.opsForValue().get(CookieUtils.getCookieValue(COOKIE_TOKEN));
        if (tokenVo == null && requestURI.startsWith("/ncnb")) {
            ResultVo<Object> resultVo = ResultVo.fail("token失效");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            resultVo.setCode(10003);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            @Cleanup
            PrintWriter writer = response.getWriter();
            writer.println(JsonUtils.objectToString(resultVo));
            return false;
        }
        HttpSession session = request.getSession();
        session.setAttribute("tokenVo", tokenVo);
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {

    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
    }
}
