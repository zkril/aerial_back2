package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.DesignCommentsMapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.pojo.DesignComments;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/designComment")
public class DesignCommentsController {
    @Autowired
    private DesignCommentsMapper designCommentsMapper;
    @Autowired
    private UsersMapper usersMapper;
    // 新增评论
    @Value("${app.image-base-url}")
    private String imageBaseUrl;
    @Value("${app.comment-image}")
    private String uploadBasePath; // 可以用 application.properties 配置


    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public Result addComment(
            @RequestParam("content") String content,
            @RequestParam("userId") Integer userId,
            @RequestPart(value = "photo", required = false) MultipartFile photo, // 允许为null
            @RequestParam("designId") Integer designId
    ) throws IOException {

        String photoPath = "";
        if (photo != null && !photo.isEmpty()) {
            // 1. 图片保存到服务器本地
            String originalFileName = photo.getOriginalFilename();
            String fileExt = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            // 用UUID防止重复
            String fileName = UUID.randomUUID() + fileExt;
            File dir = new File(uploadBasePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File saveFile = new File(dir, fileName);
            photo.transferTo(saveFile);

            // 2. 生成图片可访问的URL（前后端分离常用，或者直接存相对路径）
            photoPath = "/comment-image/" + fileName; // 静态资源可直接映射
        }

        // 3. 创建你的实体对象并写入数据库
        DesignComments comment = new DesignComments();
        comment.setIntro(content);
        comment.setUserId(userId);
        comment.setDesignId(designId);
        comment.setIsread(0);
        comment.setNotified(0);
        comment.setCreatedTime(LocalDateTime.now());
        comment.setPhoto(photoPath); // 允许为null
        designCommentsMapper.insert(comment);

        // 4. 返回成功
        return Result.ok();
    }


    // 获取指定产品的评论列表
    @GetMapping("/list/{designId}")
    public Result getComments(
            @PathVariable Integer designId,
            @RequestParam("userId") Integer userId  // 当前用户ID由前端传
    ) {
        List<DesignComments> comments = designCommentsMapper.selectList(
                new QueryWrapper<DesignComments>()
                        .eq("design_id", designId)
                        .orderByDesc("created_time")
        );
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (DesignComments comment : comments) {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("id", comment.getId());
            Users user = usersMapper.selectById(comment.getUserId());
            commentMap.put("name",user.getUsername());
            commentMap.put("avatar",imageBaseUrl+user.getAvatar());
            commentMap.put("intro", comment.getIntro());
            commentMap.put("likeNumber", comment.getLikeNumber());
            commentMap.put("time", comment.getCreatedTime());

            // 处理 photo 字段
            if (comment.getPhoto() != null && !comment.getPhoto().isEmpty()) {
                commentMap.put("photo", imageBaseUrl+comment.getPhoto());
            } else {
                commentMap.put("photo", null);
            }

            // liked 字段：是否当前用户点过赞
            boolean liked = false;
            String likedUserIds = comment.getLikedUserIds();
            if (likedUserIds != null && !likedUserIds.isEmpty()) {
                List<String> likedUserIdList = Arrays.asList(likedUserIds.split(","));
                liked = likedUserIdList.contains(String.valueOf(userId));
            }
            commentMap.put("liked", liked);

            resultList.add(commentMap);
        }

        return Result.ok(resultList);
    }
    @PostMapping("/like")
    public Result likeComment(@RequestParam Integer commentId, @RequestParam Integer userId) {
        DesignComments comment = designCommentsMapper.selectById(commentId);
        if (comment == null) return Result.fail("评论不存在");

        String likedUserIds = comment.getLikedUserIds();
        Set<String> userIdSet = new HashSet<>();
        if (likedUserIds != null && !likedUserIds.isEmpty()) {
            userIdSet.addAll(Arrays.asList(likedUserIds.split(",")));
        }

        if (!userIdSet.contains(String.valueOf(userId))) {
            userIdSet.add(String.valueOf(userId));
            comment.setLikeNumber((comment.getLikeNumber() == null ? 0 : comment.getLikeNumber()) + 1);
            comment.setLikedUserIds(String.join(",", userIdSet));
            designCommentsMapper.updateById(comment);
        }

        return Result.ok();
    }
    @PostMapping("/unlike")
    public Result unlikeComment(@RequestParam Integer commentId, @RequestParam Integer userId) {
        DesignComments comment = designCommentsMapper.selectById(commentId);
        if (comment == null) return Result.fail("评论不存在");

        String likedUserIds = comment.getLikedUserIds();
        Set<String> userIdSet = new HashSet<>();
        if (likedUserIds != null && !likedUserIds.isEmpty()) {
            userIdSet.addAll(Arrays.asList(likedUserIds.split(",")));
        }

        if (userIdSet.contains(String.valueOf(userId))) {
            userIdSet.remove(String.valueOf(userId));
            int likeNumber = comment.getLikeNumber() == null ? 0 : comment.getLikeNumber();
            comment.setLikeNumber(Math.max(0, likeNumber - 1));
            comment.setLikedUserIds(String.join(",", userIdSet));
            designCommentsMapper.updateById(comment);
        }

        return Result.ok();
    }

    @PostMapping(value = "/reply")
    public Result replayComment(
            @RequestParam("content") String content,
            @RequestParam("userId") Integer userId,
            @RequestParam("commentId") Integer commentId
    )  {
        DesignComments comment = new DesignComments();
        comment.setIntro(content);
        comment.setUserId(userId);
        comment.setIsread(0);
        comment.setNotified(0);
        comment.setCreatedTime(LocalDateTime.now());
        comment.setReplyTo(commentId);
        designCommentsMapper.insert(comment);
        // 4. 返回成功
        return Result.ok();
    }
    // 标记评论为已读
    @PostMapping("/read/{id}")
    public Result markRead(@PathVariable Long id) {

        DesignComments comment = designCommentsMapper.selectById(id);
        if (comment != null) {
            comment.setIsread(1);
            designCommentsMapper.updateById(comment);
        }
        return Result.ok();
    }
}
