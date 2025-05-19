//package com.zkril.aerial_back;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.zkril.aerial_back.mapper.UsersMapper;
//import com.zkril.aerial_back.pojo.Users;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentMatchers;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.*;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class LoginControllerTest{
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    // 模拟 UsersMapper，供 LoginController 调用
//    @MockBean
//    private UsersMapper usersMapper;
//
//    @Test
//    void contextLoads() {
//    }
//
//    // 测试场景1：用户名不存在
//    @Test
//    void testLogin_UserNotFound() throws Exception {
//        // 模拟查询时返回 null
//        when(usersMapper.selectOne(ArgumentMatchers.any(QueryWrapper.class))).thenReturn(null);
//
//        mockMvc.perform(post("/login")
//                        .param("userName", "nonexistent")
//                        .param("passwordHash", "anything")
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
//                .andExpect(status().isOk())
//                // 根据 Result.fail("用户名不存在") 期望返回 success 为 false，msg 为 "用户名不存在"
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.msg").value("用户名不存在"));
//    }
//
//    // 测试场景2：用户名存在但密码错误
//    @Test
//    void testLogin_PasswordIncorrect() throws Exception {
//        // 构造一个用户对象，设定密码为 "correctpassword"
//        Users user = new Users();
//        user.setUserId(1);
//        user.setUsername("testuser");
//        user.setPassword("correctpassword");
//        user.setEmail("test@example.com");
//        user.setAvatar("avatar_url");
//
//        when(usersMapper.selectOne(ArgumentMatchers.any(QueryWrapper.class))).thenReturn(user);
//
//        mockMvc.perform(post("/login")
//                        .param("userName", "testuser")
//                        .param("passwordHash", "wrongpassword")
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
//                .andExpect(status().isOk())
//                // 根据 Result.fail("密码错误")
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.msg").value("密码错误"));
//    }
//
//    // 测试场景3：登录成功，返回用户信息和 token
//    @Test
//    void testLogin_Success() throws Exception {
//        // 构造一个用户对象，密码为 "correctpassword"
//        Users user = new Users();
//        user.setUserId(1);
//        user.setUsername("testuser");
//        user.setPassword("correctpassword");
//        user.setEmail("test@example.com");
//        user.setAvatar("avatar_url");
//
//        when(usersMapper.selectOne(ArgumentMatchers.any(QueryWrapper.class))).thenReturn(user);
//
//        mockMvc.perform(post("/login")
//                        .param("userName", "testuser")
//                        .param("passwordHash", "correctpassword")
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
//                .andExpect(status().isOk())
//                // 期望返回结果中的 success 为 true
//                .andExpect(jsonPath("$.success").value(true))
//                // 验证返回结果中的 user 字段的 userId 为 1
//                .andExpect(jsonPath("$.data.user.userId").value(1))
//                // 验证返回结果中 token 字段存在（不检查具体内容）
//                .andExpect(jsonPath("$.data.token").exists());
//    }
//}
