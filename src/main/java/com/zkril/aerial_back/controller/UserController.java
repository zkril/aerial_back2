package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersMapper usersMapper;
    @Value("${app.user-image}")
    private String uploadBasePath; // 可以用 application.properties 配置
    @Value("${app.image-base-url}")
    private String imageBaseUrl;
    @PostMapping(value = "/update" , consumes = "multipart/form-data")
    public Result updateUser(@RequestParam String phone, @RequestParam Integer sex
    , @RequestPart(value = "photo", required = false) MultipartFile photo, @RequestParam String userId) throws IOException {

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
            photoPath = "/user-image/" + fileName; // 静态资源可直接映射
        }

        // 2. 组织更新条件
        UpdateWrapper<Users> uw = new UpdateWrapper<>();
        uw.eq("user_id", userId);
        Users user = usersMapper.selectOne(uw);

        // 3. 只更新非空字段
        if (phone != null) { user.setPhone(phone); }
        if (sex  != null) { user.setSex(sex); }
        if (!photoPath.isEmpty()) { user.setAvatar(photoPath); }
        if (usersMapper.updateById(user) > 0) {

            return Result.ok(imageBaseUrl+user.getAvatar());
        }
        return Result.fail("保存失败，请稍后重试");
    }
}
