@file:Suppress("unused")

package org.javamaster.invocationlab.admin.handler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import org.apache.commons.collections4.MultiValuedMap
import org.apache.commons.collections4.multimap.HashSetValuedHashMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author yudong
 */
@Component

class WebSocketHandler : AbstractWebSocketHandler() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    override fun afterConnectionEstablished(session: WebSocketSession) {
        if (session.uri == null) {
            return
        }
        log.info("ws connect")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        if (session.uri == null) {
            return
        }
        val params = getParamsByUri(session.uri)
        SESSION_ID_MAP[getSessionId(params)] = session
        val txt = message.payload
        log.info("add:{},{},{}", params["projectId"], params["username"], txt)
        map.put(params["projectId"], params["username"])
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        if (session.uri == null) {
            return
        }
        val params = getParamsByUri(session.uri)
        SESSION_ID_MAP.remove(getSessionId(params))
        map.removeMapping(params["projectId"], params["username"])
        log.info("ws close:{},{}", params["projectId"], params["username"])
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        if (session.uri == null) {
            return
        }
        val params = getParamsByUri(session.uri)
        SESSION_ID_MAP.remove(getSessionId(params))
        map.removeMapping(params["projectId"], params["username"])
        log.info("ws error:{},{}", params["projectId"], params["username"])
    }

    fun syncPatch(delta: JsonNode?, currentUserid: String?, projectId: String) {
        val objectNode = objectMapper.createObjectNode()
        objectNode.put("username", currentUserid)
        objectNode.set<JsonNode>("delta", delta)

        val users = HashSet(map[projectId])
        users.remove(currentUserid)
        for (userid in users) {
            log.info("sync:{},{},{}", projectId, userid, objectNode)
            val session = SESSION_ID_MAP["$projectId:$userid"]
            session!!.sendMessage(TextMessage(objectMapper.writeValueAsString(objectNode)))
        }
    }

    fun multiEdit(projectId: String?): Boolean {
        return map[projectId].size > 1
    }

    private fun getSessionId(params: Map<String, String>): String {
        return params["projectId"] + ":" + params["username"]
    }

    private fun getParamsByUri(uri: URI?): Map<String, String> {
        val query = uri!!.query
        val split = query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val map: MutableMap<String, String> = Maps.newHashMap()
        Arrays.stream(split)
            .forEach { s: String ->
                val strings = s.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                map[strings[0]] = strings[1]
            }
        return map
    }

    companion object {
        private val map: MultiValuedMap<String?, String?> = HashSetValuedHashMap()
        private val SESSION_ID_MAP: MutableMap<String, WebSocketSession> = ConcurrentHashMap()
    }
}
