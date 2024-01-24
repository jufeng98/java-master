package org.javamaster.invocationlab.admin.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author yudong
 */
@Component
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {
    @Autowired
    private ObjectMapper objectMapper;
    private static final MultiValuedMap<String, String> map = new HashSetValuedHashMap<>();
    private static final Map<String, WebSocketSession> SESSION_ID_MAP = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        if (session.getUri() == null) {
            return;
        }
        log.info("ws connect");
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (session.getUri() == null) {
            return;
        }
        Map<String, String> params = getParamsByUri(session.getUri());
        SESSION_ID_MAP.put(getSessionId(params), session);
        String txt = message.getPayload();
        log.info("add:{},{},{}", params.get("projectId"), params.get("username"), txt);
        map.put(params.get("projectId"), params.get("username"));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (session.getUri() == null) {
            return;
        }
        Map<String, String> params = getParamsByUri(session.getUri());
        SESSION_ID_MAP.remove(getSessionId(params));
        map.removeMapping(params.get("projectId"), params.get("username"));
        log.info("ws close:{},{}", params.get("projectId"), params.get("username"));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        if (session.getUri() == null) {
            return;
        }
        Map<String, String> params = getParamsByUri(session.getUri());
        SESSION_ID_MAP.remove(getSessionId(params));
        map.removeMapping(params.get("projectId"), params.get("username"));
        log.info("ws error:{},{}", params.get("projectId"), params.get("username"));
    }

    public void syncPatch(JsonNode delta, String currentUserid, String projectId) throws Exception {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("username", currentUserid);
        objectNode.set("delta", delta);

        HashSet<String> users = new HashSet<>(map.get(projectId));
        users.remove(currentUserid);
        for (String userid : users) {
            log.info("sync:{},{},{}", projectId, userid, objectNode);
            WebSocketSession session = SESSION_ID_MAP.get(projectId + ":" + userid);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(objectNode)));
        }
    }

    public boolean multiEdit(String projectId) {
        return map.get(projectId).size() > 1;
    }

    private String getSessionId(Map<String, String> params) {
        return params.get("projectId") + ":" + params.get("username");
    }

    private Map<String, String> getParamsByUri(URI uri) {
        String query = uri.getQuery();
        String[] split = query.split("&");
        Map<String, String> map = Maps.newHashMap();
        Arrays.stream(split)
                .forEach(s -> {
                    String[] strings = s.split("=");
                    map.put(strings[0], strings[1]);
                });
        return map;
    }

}
