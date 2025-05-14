package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.ChatMessagesMapper;
import com.zkril.aerial_back.mapper.UserSessionsMapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.pojo.ChatMessages;
import com.zkril.aerial_back.pojo.UserSessions;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private UserSessionsMapper userSessionsMapper;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private ChatMessagesMapper chatMessagesMapper;

    /**
     * 创建用户会话（聊天列表项）
     * @param userIdA 当前用户
     * @param userIdB 目标用户
     */
    @PostMapping("/add")
    public Result startSession(@RequestParam Integer userIdA, @RequestParam Integer userIdB) {
        // 检查是否已存在该会话（无序）
        QueryWrapper<UserSessions> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("user1_id", userIdA).eq("user2_id", userIdB))
                .or(w -> w.eq("user1_id", userIdB).eq("user2_id", userIdA));

        UserSessions existing = userSessionsMapper.selectOne(wrapper);
        if (existing != null) {
            return Result.fail("会话已存在");
        }

        // 插入新会话
        UserSessions session = new UserSessions();
        session.setUser1Id(userIdA);
        session.setUser2Id(userIdB);
        userSessionsMapper.insert(session);

        return Result.ok("会话创建成功");
    }
    @GetMapping("/list")
    public Result getUserChatList(@RequestParam Integer userId) {
        System.out.println("/list");
        // 查询会话关系表
        List<UserSessions> sessions = userSessionsMapper.selectList(
                new QueryWrapper<UserSessions>()
                        .eq("user1_id", userId).or()
                        .eq("user2_id", userId)
        );

        List<Map<String, Object>> chatList = new ArrayList<>();

        for (UserSessions session : sessions) {
            Integer otherUserId = session.getUser1Id().equals(userId) ? session.getUser2Id() : session.getUser1Id();
            Users otherUser = usersMapper.selectById(otherUserId);

            // 获取最后一条消息
            ChatMessages lastMsg = chatMessagesMapper.selectOne(
                    new QueryWrapper<ChatMessages>()
                            .nested(q -> q.eq("from_user_id", userId).eq("to_user_id", otherUserId)
                                    .or()
                                    .eq("from_user_id", otherUserId).eq("to_user_id", userId))
                            .orderByDesc("timestamp")
                            .last("LIMIT 1")
            );

            // 获取未读数量（对方发给当前用户 & 未读）
            Long unread = chatMessagesMapper.selectCount(
                    new QueryWrapper<ChatMessages>()
                            .eq("from_user_id", otherUserId)
                            .eq("to_user_id", userId)
                            .eq("is_read", 0)
            );

            Map<String, Object> map = new HashMap<>();
            map.put("id", otherUser.getUserId());
            map.put("name", otherUser.getUsername());
            map.put("avatar", otherUser.getAvatar());
            map.put("lastMessage", lastMsg != null ? lastMsg.getContent() : "");
            map.put("lastTime", lastMsg != null ? lastMsg.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
            map.put("unread", unread);

            chatList.add(map);
        }

        // 按时间降序
        chatList.sort((a, b) -> {
            Long t1 = (Long) a.get("lastTime");
            Long t2 = (Long) b.get("lastTime");
            return t2 == null ? -1 : t1 == null ? 1 : t2.compareTo(t1);
        });
        System.out.println(chatList);
        return Result.ok(chatList);
    }
    @PostMapping("/read")
    public Result markMessagesAsRead(
            @RequestParam Integer userId,
            @RequestParam Integer fromUserId) {

        // 更新所有未读消息（对方发给当前用户）
        ChatMessages updateObj = new ChatMessages();
        updateObj.setIsRead(1);

        int updated = chatMessagesMapper.update(
                updateObj,
                new QueryWrapper<ChatMessages>()
                        .eq("from_user_id", fromUserId)
                        .eq("to_user_id", userId)
                        .eq("is_read", 0)
        );

        return Result.ok("成功标记 " + updated + " 条消息为已读");
    }
    @GetMapping("/history")
    public Result getMessageWithUser(
            @RequestParam Integer userId,
            @RequestParam Integer fromUserId) {

        List<ChatMessages> messages = chatMessagesMapper.selectList(
                new QueryWrapper<ChatMessages>()
                        .nested(q -> q.eq("from_user_id", userId).eq("to_user_id", fromUserId)
                                .or()
                                .eq("from_user_id", fromUserId).eq("to_user_id", userId))
                        .orderByAsc("timestamp")
        );

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (ChatMessages msg : messages) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("id", msg.getId());
            msgMap.put("content", msg.getContent());
            msgMap.put("timestamp", msg.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            msgMap.put("isSelf", msg.getFromUserId().equals(userId)); // 判断是否自己发的
            resultList.add(msgMap);
        }

        return Result.ok(resultList);
    }



}
