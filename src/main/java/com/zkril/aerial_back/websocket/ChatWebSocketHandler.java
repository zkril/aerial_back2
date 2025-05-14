package com.zkril.aerial_back.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zkril.aerial_back.mapper.ChatMessagesMapper;
import com.zkril.aerial_back.pojo.ChatMessages;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Autowired
    private ChatMessagesMapper chatMessagesMapper;
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery(); // e.g., ?userId=1
        Integer userId = Integer.parseInt(query.split("=")[1]);
        userSessions.put(userId, session);
        System.out.println("用户上线：" + userId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        // 1. 解析 JSON 为 ChatMessages 实体
        System.out.println(textMessage.getPayload());
        ChatMessages message = mapper.readValue(textMessage.getPayload(), ChatMessages.class);

        // 2. 设置补充字段
        message.setTimestamp(LocalDateTime.now());
        message.setIsRead(0);
        message.setType("personal");

        // 3. 存入数据库
        chatMessagesMapper.insert(message);

        // 4. 推送消息给接收者
        WebSocketSession toSession = userSessions.get(message.getToUserId());
        if (toSession != null && toSession.isOpen()) {
            System.out.println(new TextMessage(mapper.writeValueAsString(message)));
            toSession.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        }

        // 5. 给发送者确认
        Map<String, Object> data = new HashMap<>();
        data.put("toUserId", message.getToUserId());
        data.put("content", message.getContent());
        data.put("timestamp", message.getTimestamp());
        data.put("fromUserId", message.getFromUserId());
        data.put("messageId", message.getId());
        String json = mapper.writeValueAsString(Result.ok(data));
        System.out.println(json);
        session.sendMessage(new TextMessage(json));
    }

    public WebSocketSession getSessionByUserId(Integer userId) {
        return userSessions.get(userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        userSessions.values().remove(session);
    }
}
