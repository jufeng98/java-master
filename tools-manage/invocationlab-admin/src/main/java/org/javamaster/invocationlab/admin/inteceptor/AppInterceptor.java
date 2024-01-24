package org.javamaster.invocationlab.admin.inteceptor;

import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.consts.ErdConst;
import org.javamaster.invocationlab.admin.model.ResultVo;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService;
import org.javamaster.invocationlab.admin.util.CookieUtils;
import org.javamaster.invocationlab.admin.util.JsonUtils;
import org.javamaster.invocationlab.admin.util.SpringUtils;
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils;
import com.google.common.collect.Sets;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
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
import java.util.Set;

import static org.javamaster.invocationlab.admin.consts.ErdConst.ADMIN_CODE;
import static org.javamaster.invocationlab.admin.consts.ErdConst.COOKIE_TOKEN;


/**
 * @author yudong
 */
public class AppInterceptor extends HandlerInterceptorAdapter implements HandlerInterceptor {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String indexContent;
    private final ErdOnlineUserService erdOnlineUserService;

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

    public AppInterceptor(ErdOnlineUserService erdOnlineUserService) {
        this.erdOnlineUserService = erdOnlineUserService;
    }

    @SuppressWarnings("NullableProblems")
    @SneakyThrows
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.info("开始处理:{}", request.getRequestURI());

        ThreadLocalUtils.set("startTime", System.currentTimeMillis());

        if (requestURI.contains("/actuator")) {
            return true;
        }

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
        } else if (requestURI.equals("/logout")) {
            response.sendRedirect("/invocationlab-erd-online-view/login");
            return false;
        }

        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = (RedisTemplate<String, Object>) SpringUtils.getContext()
                .getBean("redisTemplateJackson");
        String token = CookieUtils.getCookieValue(COOKIE_TOKEN);
        TokenVo tokenVo = (TokenVo) redisTemplate.opsForValue().get(token);

        if (tokenVo != null && requestURI.startsWith("/ncnb/project/recent")) {
            erdOnlineUserService.refreshToken(token);
        }

        if (tokenVo == null && requestURI.startsWith("/ncnb")) {
            return tokenExpireResponse(response);
        }

        if (tokenVo == null && SpringUtils.isProEnv()
                && (requestURI.startsWith("/dubbo") || requestURI.startsWith("/redis") || requestURI.contains("/getUserInfo"))) {
            response.addHeader("ajax-header", "sessiontimeout");
            return false;
        }

        if (SpringUtils.isProEnv() && (requestURI.startsWith("/dubbo") || requestURI.startsWith("/redis"))) {
            StringRedisTemplate stringRedisTemplate = SpringUtils.getContext().getBean(StringRedisTemplate.class);
            Set<String> members = stringRedisTemplate.opsForSet().members(ErdConst.ANGEL_PRO_RPC_ALLOW);
            if (CollectionUtils.isEmpty(members)) {
                members = Sets.newHashSet(ADMIN_CODE);
                stringRedisTemplate.opsForSet().add(ErdConst.ANGEL_PRO_RPC_ALLOW, ADMIN_CODE);
            }
            //noinspection ConstantConditions
            if (!members.contains(tokenVo.getUserId())) {
                throw new ErdException("无权访问");
            }
        }

        HttpSession session = request.getSession();
        session.setAttribute("tokenVo", tokenVo);
        return true;
    }

    private boolean tokenExpireResponse(HttpServletResponse response) throws Exception {
        ResultVo<Object> resultVo = ResultVo.fail("token失效");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        resultVo.setCode(10003);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        @Cleanup
        PrintWriter writer = response.getWriter();
        writer.println(JsonUtils.objectToString(resultVo));
        return false;
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
        long startTime = ThreadLocalUtils.get("startTime");
        ThreadLocalUtils.remove("startTime");
        log.info("处理完成:{},耗时:{}ms", request.getRequestURI(), System.currentTimeMillis() - startTime);
    }
}
