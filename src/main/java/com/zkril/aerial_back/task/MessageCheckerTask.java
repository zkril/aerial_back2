//package com.zkril.aerial_back.task;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.zkril.aerial_back.mapper.ChatMessagesMapper;
//import com.zkril.aerial_back.pojo.ChatMessages;
//import com.zkril.aerial_back.websocket.ChatWebSocketHandler;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//import java.lang.management.PlatformLoggingMXBean;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class MessageCheckerTask {
//
//    @Autowired
//    private ChatMessagesMapper chatMessagesMapper;
//
//    @Autowired
//    private ChatWebSocketHandler chatWebSocketHandler;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    /**
//     * 每5秒检测未读消息
//     */
//    @Scheduled(fixedRate = 5000)
//    public void checkUnreadMessages() {
//        QueryWrapper<ChatMessages> query = new QueryWrapper<>();
//        query.eq("is_read", 0); // 0代表未读
//
//        List<ChatMessages> unreadList = chatMessagesMapper.selectList(query);
//
//        for (ChatMessages msg : unreadList) {
//            Integer toUserId = msg.getToUserId();
//
//            // 获取对应用户的 WebSocketSession
//            WebSocketSession session = chatWebSocketHandler.getSessionByUserId(toUserId);
//            if (session != null && session.isOpen()) {
//                try {
//                    Map<String, Object> payload = new HashMap<>();
//                    payload.put("id", msg.getId());
//                    payload.put("title", msg.getTitle());
//                    payload.put("content", msg.getContent());
//                    payload.put("time",msg.getTimestamp().toString());
//                    session.sendMessage(new TextMessage(mapper.writeValueAsString(payload)));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//}
