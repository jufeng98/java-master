package org.javamaster.invocationlab.admin.aspect

import org.javamaster.invocationlab.admin.annos.ErdRolesAllowed
import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.enums.RoleEnum
import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.service.ErdOnlineRoleService
import org.javamaster.invocationlab.admin.util.SpringUtils
import com.google.common.collect.Sets
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*
import java.util.stream.Collectors

/**
 * @author yudong
 */
@Aspect
@Component
class ErdRolesAllowedAspect {

    @Around("@annotation(org.javamaster.invocationlab.admin.annos.ErdRolesAllowed)")
    fun checkUserRole(joinPoint: ProceedingJoinPoint): Any {
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request = requestAttributes.request
        val projectId = request.getHeader("projectId")
        val session = request.session
        val tokenVo = session.getAttribute("tokenVo") as TokenVo

        val erdOnlineRoleService = SpringUtils.context.getBean(ErdOnlineRoleService::class.java)
        val pair = erdOnlineRoleService.getUserRoleIds(tokenVo.userId!!, projectId)
        val userRoleIds = pair.key
        val args = joinPoint.args
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val rolesAllowed = method.getAnnotation(ErdRolesAllowed::class.java)
        val methodRoleIds = Arrays.stream(rolesAllowed.value)
            .map { roleEnum: RoleEnum -> roleEnum.id }
            .collect(Collectors.toSet())
        if (Collections.disjoint(Sets.newHashSet(userRoleIds), methodRoleIds)) {
            throw ErdException(rolesAllowed.msg)
        }
        return joinPoint.proceed(args)
    }
}
