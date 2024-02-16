package org.javamaster.invocationlab.admin.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * @author yudong
 * @date 2023/6/11
 */
@Configuration
class WebSocketConfig : WebSocketConfigurer {
    @Autowired
    private lateinit var webSocketHandler: WebSocketHandler

    override fun registerWebSocketHandlers(webSocketHandlerRegistry: WebSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(webSocketHandler, "/erdSyncProject").setAllowedOrigins("*")
    }
}
