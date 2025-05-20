package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.mapper.FoldersMapper;
import com.zkril.aerial_back.pojo.Folders;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.service.MailService;
import com.zkril.aerial_back.util.JWTUtils;
import com.zkril.aerial_back.util.Result;
import com.zkril.aerial_back.util.VerifyCodeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class LoginController {

    @Autowired
    UsersMapper usersMapper;
    @Autowired
    FoldersMapper foldersMapper;
    @Value("${app.image-base-url}")
    private String imageBaseUrl;
    //登录
    @PostMapping("/login")
    public Result login(@RequestParam String userName, @RequestParam String passwordHash) {
        System.out.println("/login");
        QueryWrapper<Users> usersQueryWrapper=new QueryWrapper<>();
        usersQueryWrapper.eq("email", userName);
        Users users=usersMapper.selectOne(usersQueryWrapper);
        if (users == null) {
            return Result.fail("用户邮件不存在");
        }
        else {
            if (!passwordHash.equals(users.getPassword())) {
                return Result.fail("密码错误");
            }
            else {
                // 登录成功后生成 JWT
                // 准备 token 的 payload（可以根据业务加入更多信息）
                Map<String, String> payload = new HashMap<>();
                payload.put("userId", users.getUserId().toString());
                payload.put("userName", users.getUsername());
                payload.put("avatar", users.getAvatar());
                // 调用 JWTUtils 生成 token（此处创建一个工具类实例）
                JWTUtils jwtUtils = new JWTUtils();
                String token = jwtUtils.getToken(payload);
                LambdaQueryWrapper<Folders> foldersQueryWrapper = new LambdaQueryWrapper<>();
                foldersQueryWrapper
                        .eq(Folders::getUserId, users.getUserId())
                        .eq(Folders::getName, "默认文件夹");
                Folders folder = foldersMapper.selectOne(foldersQueryWrapper);

                // 封装返回结果，将用户信息和生成的 token 一同返回
                Map<String, Object> data = new HashMap<>();
                users.setPassword(null);
                users.setAvatar(imageBaseUrl+users.getAvatar());
                data.put("user", users);
                data.put("folder", folder.getId());
                data.put("token", token);
                return Result.ok(data);
            }
        }
    }
    //注册
    @PostMapping("/register")
    public Result checkIn(
            @RequestParam String userName,
            @RequestParam String email,
            @RequestParam String passwordHash,
            @RequestParam String confirmPassword,
            @RequestParam String code) {

        if (userName == null || passwordHash == null || confirmPassword == null || email == null || code == null
                || userName.isEmpty() || passwordHash.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || code.isEmpty()) {
            return Result.fail("缺少必要的字段");
        }

        if (!passwordHash.equals(confirmPassword)) {
            return Result.fail("两次密码输入不一致");
        }

        // 校验验证码
        if (!cache.verify(email, code)) {
            return Result.fail("验证码错误或已过期");
        }

        if (!userName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9]{2,20}$")) {
            return Result.fail("用户名格式不正确");
        }

        if (!email.matches("^([a-zA-Z0-9]+[-_.]?)+@[a-zA-Z0-9]+\\.[a-z]+$")) {
            return Result.fail("邮箱格式不正确");
        }

//        if (!passwordHash.matches("((?=.*\\d)(?=.*\\D)|(?=.*[a-zA-Z])(?=.*[^a-zA-Z]))(?!^.*[\\u4E00-\\u9FA5].*$)^\\S{6,22}$")) {
//            return Result.fail("密码格式不正确");
//        }
        if (!passwordHash.matches("^[\\x21-\\x7E]{6,18}$")) {
            return Result.fail("密码格式不正确");
        }

        // 校验唯一性
        if (usersMapper.selectOne(new QueryWrapper<Users>().eq("userName", userName)) != null) {
            return Result.fail("用户名已被注册");
        }

        if (usersMapper.selectOne(new QueryWrapper<Users>().eq("email", email)) != null) {
            return Result.fail("邮箱已被注册");
        }

        Users user = new Users();
        user.setUsername(userName);
        user.setPassword(passwordHash);
        user.setEmail(email);
        user.setIsAdmin(0);

        int inserted = usersMapper.insert(user);

        if (inserted > 0) {
            // 注册成功，创建默认文件夹
            Integer userId = user.getUserId();  // 获取自增的user_id

            // 构造默认文件夹
            Folders folder = new Folders();
            folder.setUserId(userId);
            folder.setName("默认文件夹");
            folder.setCreateTime(LocalDateTime.now());
            folder.setUpdateTime(LocalDateTime.now());

            foldersMapper.insert(folder); // 新增文件夹

            cache.remove(email); // 注册成功后删除验证码
            return Result.ok("注册成功");
        } else {
            return Result.fail("注册失败");
        }
    }
    @Autowired
    private MailService mailService;
    @Autowired
    private VerifyCodeCache cache;

    @PostMapping("/send_code")
    public Result sendCode(@RequestParam String email) {
        System.out.println("/send_code");
        if (email == null || !email.matches("^([a-zA-Z0-9]+[-_.]?)+@[a-zA-Z0-9]+\\.[a-z]+$")) {
            return Result.fail("邮箱格式不正确");
        }
        String code = String.valueOf(new Random().nextInt(899999) + 100000); // 6位验证码
        cache.save(email, code);
        mailService.sendCode(email, code);
        return Result.ok("验证码已发送");
    }
    @PostMapping("/forgot/send_code")
    public Result sendResetCode(@RequestParam String email) {
        if (email == null || !email.matches("^([a-zA-Z0-9]+[-_.]?)+@[a-zA-Z0-9]+\\.[a-z]+$")) {
            return Result.fail("邮箱格式不正确");
        }

        // 检查是否存在该用户邮箱
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        Users user = usersMapper.selectOne(wrapper);
        if (user == null) {
            return Result.fail("该邮箱未注册");
        }

        // 发送验证码
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        cache.save(email, code);
        mailService.sendCode(email, code);
        return Result.ok("验证码已发送");
    }
    @PostMapping("/forgot/reset_password")
    public Result resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword
    ) {
        System.out.println("/forgot/reset_password");
        // 1. 参数校验
        if (email == null || code == null || newPassword == null ||
                email.isEmpty() || code.isEmpty() || newPassword.isEmpty()) {
            return Result.fail("缺少必要参数");
        }

        // 2. 校验验证码（5分钟内有效）
        if (!cache.verify(email, code)) {
            return Result.fail("验证码无效或已过期");
        }

        // 3. 校验密码格式（ASCII 可见字符，6-18位）
        if (!newPassword.matches("^[\\x21-\\x7E]{6,18}$")) {
            return Result.fail("密码格式不正确");
        }

        // 4. 查找用户
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        Users user = usersMapper.selectOne(queryWrapper);

        if (user == null) {
            return Result.fail("邮箱未注册");
        }

        // 5. 更新密码
        user.setPassword(newPassword);  // 建议加密后存储
        int updated = usersMapper.updateById(user);

        // 6. 清除验证码
        cache.remove(email);
        System.out.println("/success");
        return updated > 0 ? Result.ok("密码重置成功") : Result.fail("密码重置失败");
    }


}
