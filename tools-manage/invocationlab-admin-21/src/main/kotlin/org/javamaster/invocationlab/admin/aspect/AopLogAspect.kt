package org.javamaster.invocationlab.admin.aspect

import org.javamaster.invocationlab.admin.config.GlobalLog.Companion.log
import org.javamaster.invocationlab.admin.util.ParamUtils.getParamString
import org.javamaster.invocationlab.admin.util.ParamUtils.getParamsString
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * @author yudong
 */
@Aspect
@Order(2)
@Component
class AopLogAspect {
    /**
     * 记录方法的入参和出参
     */
    @Around("@annotation(org.javamaster.invocationlab.admin.annos.AopLog)")
    @Throws(Throwable::class)
    fun recordLog(joinPoint: ProceedingJoinPoint): Any {
        val parameters: Array<Any> = joinPoint.args
        val classMethod: String = joinPoint.signature.declaringType.getSimpleName() + "#" + joinPoint.signature.name
        log.info("aop req:{},args:{}", classMethod, getParamsString(parameters))
        val resObj: Any = joinPoint.proceed(parameters)
        log.info("aop req:{},res:{}", classMethod, getParamString(resObj))
        return resObj
    }
}
