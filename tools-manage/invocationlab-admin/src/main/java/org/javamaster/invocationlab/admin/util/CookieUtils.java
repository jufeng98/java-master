package org.javamaster.invocationlab.admin.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
@SuppressWarnings("DataFlowIssue")
public class CookieUtils {

    public static String getCookieValue(String name) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        List<Cookie> list = Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return "";
        }
        return list.get(0).getValue();
    }
}
