package org.javamaster.invocationlab.admin.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Objects;

public class SessionUtils {

    public static <T> void saveToSession(String key, T obj) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpSession session = Objects.requireNonNull(requestAttributes).getRequest().getSession();
        session.setAttribute(key, obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFromSession(String key) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpSession session = Objects.requireNonNull(requestAttributes).getRequest().getSession();
        return (T) session.getAttribute(key);
    }
}
