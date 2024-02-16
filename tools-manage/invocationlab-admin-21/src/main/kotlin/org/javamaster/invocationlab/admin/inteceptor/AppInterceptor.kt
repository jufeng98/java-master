package org.javamaster.invocationlab.admin.inteceptor

import org.javamaster.invocationlab.admin.config.ErdException
import org.javamaster.invocationlab.admin.consts.ErdConst
import org.javamaster.invocationlab.admin.consts.ErdConst.ADMIN_CODE
import org.javamaster.invocationlab.admin.consts.ErdConst.COOKIE_TOKEN
import org.javamaster.invocationlab.admin.model.ResultVo
import org.javamaster.invocationlab.admin.model.erd.TokenVo
import org.javamaster.invocationlab.admin.service.ErdOnlineUserService
import org.javamaster.invocationlab.admin.util.CookieUtils
import org.javamaster.invocationlab.admin.util.JsonUtils
import org.javamaster.invocationlab.admin.util.SpringUtils
import org.javamaster.invocationlab.admin.util.ThreadLocalUtils
import com.google.common.collect.Sets
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.util.CollectionUtils
import org.springframework.util.StreamUtils
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.nio.charset.StandardCharsets

/**
 * @author yudong
 */
class AppInterceptor : HandlerInterceptor {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestURI = request.requestURI
        log.info("开始处理:{}", request.requestURI)

        ThreadLocalUtils.set("startTime", System.currentTimeMillis())

        if (requestURI.contains("/actuator")) {
            return true
        }

        for (path in paths) {
            if (requestURI.startsWith(path)) {
                // 支持react的BrowserRouter
                response.characterEncoding = StandardCharsets.UTF_8.name()
                response.contentType = MediaType.TEXT_HTML_VALUE
                response.writer.use {
                    it.println(indexContent)
                }
                return false
            }
        }

        if (requestURI == "/") {
            response.sendRedirect("/invocationlab-rpcpostman-view/index.html")
            return false
        } else if (requestURI == "/logout") {
            response.sendRedirect("/invocationlab-erd-online-view/login")
            return false
        }

        val redisTemplate = SpringUtils.context
            .getBean("redisTemplateJackson") as RedisTemplate<*, *>
        val token = CookieUtils.getCookieValue(COOKIE_TOKEN)
        val tokenVo = redisTemplate.opsForValue()[token] as TokenVo?

        if (tokenVo != null && requestURI.startsWith("/ncnb/project/recent")) {
            val erdOnlineUserService = SpringUtils.context.getBean(ErdOnlineUserService::class.java)
            erdOnlineUserService.refreshToken(token)
        }

        if (tokenVo == null && requestURI.startsWith("/ncnb")) {
            return tokenExpireResponse(response)
        }

        if (tokenVo == null && SpringUtils.proEnv
            && (requestURI.startsWith("/dubbo") || requestURI.startsWith("/redis") || requestURI.contains("/getUserInfo"))
        ) {
            response.addHeader("ajax-header", "sessiontimeout")
            return false
        }

        if (SpringUtils.proEnv && (requestURI.startsWith("/dubbo") || requestURI.startsWith("/redis"))) {
            val stringRedisTemplate = SpringUtils.context.getBean(
                StringRedisTemplate::class.java
            )
            var members = stringRedisTemplate.opsForSet().members(ErdConst.ANGEL_PRO_RPC_ALLOW)
            if (CollectionUtils.isEmpty(members)) {
                members = Sets.newHashSet(ADMIN_CODE)
                stringRedisTemplate.opsForSet().add(ErdConst.ANGEL_PRO_RPC_ALLOW, ADMIN_CODE)
            }
            if (!members!!.contains(tokenVo!!.userId)) {
                throw ErdException("无权访问")
            }
        }

        val session = request.session
        session.setAttribute("tokenVo", tokenVo)
        return true
    }

    private fun tokenExpireResponse(response: HttpServletResponse): Boolean {
        val resultVo = ResultVo.fail<Any>("token失效")
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        resultVo.code = 10003
        response.characterEncoding = StandardCharsets.UTF_8.name()
        response.writer.use {
            it.println(JsonUtils.objectToString(resultVo))
        }
        return false
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse, handler: Any,
        modelAndView: ModelAndView?
    ) {
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse, handler: Any, ex: Exception?
    ) {
        val startTime = ThreadLocalUtils.get<Long>("startTime")!!
        ThreadLocalUtils.remove("startTime")
        log.info("处理完成:{},耗时:{}ms", request.requestURI, System.currentTimeMillis() - startTime)
    }

    companion object {
        private var indexContent: String? = null

        init {
            try {
                val path = "/public/invocationlab-erd-online-view/index.html"
                AppInterceptor::class.java.getResourceAsStream(path).use {
                    indexContent = StreamUtils.copyToString(it, StandardCharsets.UTF_8)
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        private val paths = arrayOf(
            "/invocationlab-erd-online-view/index.html",
            "/invocationlab-erd-online-view/login",
            "/invocationlab-erd-online-view/project",
            "/invocationlab-erd-online-view/design"
        )
    }
}
