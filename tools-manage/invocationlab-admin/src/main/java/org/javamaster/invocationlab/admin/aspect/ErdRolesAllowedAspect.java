package org.javamaster.invocationlab.admin.aspect;

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed;
import org.javamaster.invocationlab.admin.config.ErdException;
import org.javamaster.invocationlab.admin.enums.RoleGroupEnum;
import org.javamaster.invocationlab.admin.model.erd.TokenVo;
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yudong
 */
@Aspect
@Component
public class ErdRolesAllowedAspect {
    @Autowired
    private ErdOnlineRoleService erdOnlineRoleService;

    @SuppressWarnings({"DataFlowIssue", "ConstantConditions"})
    @Around("@annotation(org.javamaster.invocationlab.admin.annos.ErdRolesAllowed)")
    public Object checkUserRole(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        @SuppressWarnings("DataFlowIssue")
        HttpServletRequest request = requestAttributes.getRequest();
        String projectId = request.getHeader("projectId");
        HttpSession session = request.getSession();
        TokenVo tokenVo = (TokenVo) session.getAttribute("tokenVo");

        Pair<Set<Long>, RoleGroupEnum> pair = erdOnlineRoleService.getUserRoleIds(tokenVo.getUserId(), projectId);
        Set<Long> userRoleIds = pair.getKey();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ErdRolesAllowed rolesAllowed = method.getAnnotation(ErdRolesAllowed.class);
        Set<Long> methodRoleIds = Arrays.stream(rolesAllowed.value())
                .map(roleEnum -> roleEnum.id)
                .collect(Collectors.toSet());
        if (Collections.disjoint(Sets.newHashSet(userRoleIds), methodRoleIds)) {
            throw new ErdException(rolesAllowed.msg());
        }
        return joinPoint.proceed(args);
    }

}
