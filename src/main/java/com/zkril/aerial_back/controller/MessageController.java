package com.zkril.aerial_back.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.ChatMessagesMapper;
import com.zkril.aerial_back.pojo.ChatMessages;
import com.zkril.aerial_back.util.JWTUtils;
import com.zkril.aerial_back.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    ChatMessagesMapper chatMessagesMapper;
    @GetMapping("/unread")
    public Result getUnreadMessages(@RequestParam String userId) {

        List<ChatMessages> unreadMessages = chatMessagesMapper.selectList(
                new QueryWrapper<ChatMessages>()
                        .eq("to_user_id", userId)
                        .eq("is_read", 0)
        );

        Map<String, List<ChatMessages>> grouped = unreadMessages.stream()
                .collect(Collectors.groupingBy(ChatMessages::getType));

        return Result.ok(grouped); // 返回按类型分组的未读消息
    }
    @PostMapping("/read")
    public Result markAsRead(@RequestParam List<Integer> messageIds) {
        for (Integer id : messageIds) {
            ChatMessages msg = new ChatMessages();
            msg.setId(id);
            msg.setIsRead(1);
            chatMessagesMapper.updateById(msg);
        }
        return Result.ok("已标记为已读");
    }

}
