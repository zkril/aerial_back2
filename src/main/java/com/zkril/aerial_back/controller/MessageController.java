package com.zkril.aerial_back.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.ChatMessagesMapper;
import com.zkril.aerial_back.mapper.ProductCommentsMapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.pojo.ChatMessages;
import com.zkril.aerial_back.pojo.ProductComments;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.util.JWTUtils;
import com.zkril.aerial_back.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class MessageController {
    @Autowired
    ChatMessagesMapper chatMessagesMapper;
    @Autowired
    ProductCommentsMapper productCommentsMapper;
    @Autowired
    UsersMapper usersMapper;
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

    @GetMapping("/interactions")
    public Result getInteractions(@RequestParam("userId") Integer userId) {
        Users user = usersMapper.selectById(userId);
        if (user == null) return Result.ok(Collections.emptyList());

        // 1. 获取当前用户所有评论 ID
        List<Integer> myCommentIds = productCommentsMapper.selectList(
                new QueryWrapper<ProductComments>().eq("user_id", userId)
        ).stream().map(ProductComments::getId).collect(Collectors.toList());

        if (myCommentIds.isEmpty()) return Result.ok(Collections.emptyList());

        // 2. 查询所有回复我评论的记录
        List<ProductComments> replies = productCommentsMapper.selectList(
                new QueryWrapper<ProductComments>().in("reply_to", myCommentIds)
        );
        if (replies.isEmpty()) return Result.ok(Collections.emptyList());

        // 3. 批量查询涉及的用户
        Set<Integer> userIds = replies.stream()
                .map(ProductComments::getUserId)
                .collect(Collectors.toSet());
        Map<Integer, Users> userMap = userIds.isEmpty()
                ? new HashMap<>()
                : usersMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(Users::getUserId, Function.identity()));

        // 4. 批量查询当前用户所有原始评论（Map化）
        Map<Integer, ProductComments> myCommentMap = productCommentsMapper.selectBatchIds(myCommentIds)
                .stream().collect(Collectors.toMap(ProductComments::getId, Function.identity()));

        // 5. 批量查询所有二层楼中楼评论（提前查好）
        Set<Integer> upperCommentIds = myCommentMap.values().stream()
                .filter(c -> c.getReplyTo() != null)
                .map(ProductComments::getReplyTo)
                .collect(Collectors.toSet());
        Map<Integer, ProductComments> upperCommentMap = upperCommentIds.isEmpty()
                ? new HashMap<>()
                : productCommentsMapper.selectBatchIds(upperCommentIds).stream()
                .collect(Collectors.toMap(ProductComments::getId, Function.identity()));

        // 6. 拼接结果
        List<Map<String, Object>> data = new ArrayList<>();
        for (ProductComments reply : replies) {
            Map<String, Object> item = new HashMap<>();
            Users sender = userMap.get(reply.getUserId());

            item.put("id", reply.getId());
            item.put("time", reply.getCreatedTime());
            item.put("sendName", sender != null ? sender.getUsername() : "未知用户");
            item.put("sendPhoto", sender != null ? sender.getAvatar() : null);
            item.put("sentence", "回复 @" + user.getUsername() + ": " + reply.getIntro());

            ProductComments myComment = myCommentMap.get(reply.getReplyTo());

            if (myComment != null) {
                ProductComments upper = myComment.getReplyTo() != null
                        ? upperCommentMap.get(myComment.getReplyTo())
                        : null;

                if (upper != null) {
                    item.put("sentenceYours", user.getUsername() + ": " + myComment.getIntro());
                    item.put("sentenceRow", upper.getIntro());
                } else {
                    item.put("sentenceYours", "");
                    item.put("sentenceRow", myComment.getIntro());
                }
            } else {
                item.put("sentenceYours", "");
                item.put("sentenceRow", "");
            }

            data.add(item);
        }

        return Result.ok(data);
    }



}
