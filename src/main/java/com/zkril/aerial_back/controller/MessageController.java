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
        // 1. 查询我发过的评论ID
        List<Integer> myCommentIds = productCommentsMapper.selectList(
                new QueryWrapper<ProductComments>().eq("user_id", userId)
        ).stream().map(ProductComments::getId).collect(Collectors.toList());

        if (myCommentIds.isEmpty()) return Result.ok(Collections.emptyList());

        // 2. 查询回复了我的评论的所有评论
        List<ProductComments> replies = productCommentsMapper.selectList(
                new QueryWrapper<ProductComments>().in("reply_to", myCommentIds)
        );

        // 3. 查询所有涉及的用户
        Set<Integer> userIds = replies.stream().map(ProductComments::getUserId).collect(Collectors.toSet());

        Map<Integer, Users> userMap = usersMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(Users::getUserId, Function.identity()));

        // 4. 查询我的评论Map
        Map<Integer, ProductComments> myCommentMap = productCommentsMapper.selectBatchIds(myCommentIds)
                .stream().collect(Collectors.toMap(ProductComments::getId, Function.identity()));

        // 5. 拼装结构
        List<Map<String, Object>> data = new ArrayList<>();
        for (ProductComments reply : replies) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", reply.getId());
            item.put("time", reply.getCreatedTime());
            item.put("sendName", userMap.get(reply.getUserId()).getUsername());
            item.put("sendPhoto", userMap.get(reply.getUserId()).getAvatar());
            item.put("sentence", "回复 @"+user.getUsername()+": "+reply.getIntro());

            ProductComments myComment = myCommentMap.get(reply.getReplyTo());


            // 楼中楼查sentenceYours
            String sentenceYours = "";
            if (myComment != null && myComment.getReplyTo() != null) {
                // 查更上一层评论（C）
                ProductComments upperComment = productCommentsMapper.selectById(myComment.getReplyTo());
                if (upperComment != null) {
                    sentenceYours = upperComment.getIntro();
                }
            }
            if(sentenceYours.isEmpty()){
                item.put("sentenceYours", sentenceYours);
                item.put("sentenceRow", myComment != null ? myComment.getIntro() : "");
            }
           else {
                item.put("sentenceYours", myComment != null ? user.getUsername()+": "+myComment.getIntro() : "");
                item.put("sentenceRow", sentenceYours);
            }

            data.add(item);
        }
        return Result.ok(data);
    }


}
