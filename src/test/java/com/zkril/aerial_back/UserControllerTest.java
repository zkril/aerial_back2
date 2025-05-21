package com.zkril.aerial_back;

import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.pojo.Users;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController测试类。
 * 测试UserController的updateUser接口，包括成功更新用户信息（分别在无上传照片和有上传照片情况下）以及失败场景（更新失败、缺少参数）。
 */
@SpringBootTest(properties = {"app.user-image=./test-uploads", "app.image-base-url=http://test-base"})
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UsersMapper usersMapper;

    // 测试用户资料更新成功（无照片上传），期望返回code=200，data为用户头像完整URL，message为“成功”
    @Test
    void testUpdateUserSuccessWithoutPhoto() throws Exception {
        // 模拟数据库中存在对应用户，并成功更新
        Users existingUser = new Users();
        existingUser.setUserId(1);
        existingUser.setAvatar("/oldAvatar.png");
        when(usersMapper.selectOne(any())).thenReturn(existingUser);
        when(usersMapper.updateById(any())).thenReturn(1);

        // 执行POST请求（不上传照片）
        mockMvc.perform(multipart("/user/update")
                                .param("userId", "1")
                                .param("phone", "13800138000")
                                .param("sex", "1")  // sex=1
                        // 不传递photo文件，模拟无头像上传
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                // 返回的data应为imageBaseUrl + 用户当前头像路径
                .andExpect(jsonPath("$.data").value("http://test-base/oldAvatar.png"));
    }

    // 测试用户资料更新成功（包含照片上传），期望返回code=200，data为新头像完整URL，message为“成功”
    @Test
    void testUpdateUserSuccessWithPhoto() throws Exception {
        // 模拟数据库中存在对应用户，并成功更新
        Users existingUser = new Users();
        existingUser.setUserId(2);
        existingUser.setAvatar("/oldAvatar2.png");
        when(usersMapper.selectOne(any())).thenReturn(existingUser);
        when(usersMapper.updateById(any())).thenReturn(1);

        // 准备模拟上传的照片文件
        byte[] fileContent = "dummyimage".getBytes();
        MockMultipartFile photoFile = new MockMultipartFile("photo", "test.jpg", "image/jpeg", fileContent);

        // 执行带文件的POST请求
        mockMvc.perform(multipart("/user/update")
                        .file(photoFile)
                        .param("userId", "2")
                        .param("phone", "13900139000")
                        .param("sex", "0")  // sex=0
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                // 验证返回的data字段包含新生成的头像URL路径（以/user-image/开头并以.jpg结尾）
                .andExpect(jsonPath("$.data", containsString("/user-image/")));
    }

    // 测试用户资料更新失败（更新操作返回0），期望返回code=201，message为“保存失败，请稍后重试”，data为null
    @Test
    void testUpdateUserFailUpdateFailed() throws Exception {
        // 模拟数据库中存在对应用户，但更新返回0行受影响
        Users existingUser = new Users();
        existingUser.setUserId(3);
        existingUser.setAvatar("/avatar3.png");
        when(usersMapper.selectOne(any())).thenReturn(existingUser);
        when(usersMapper.updateById(any())).thenReturn(0);

        // 执行POST请求（无照片上传），更新将会失败
        mockMvc.perform(multipart("/user/update")
                        .param("userId", "3")
                        .param("phone", "13700137000")
                        .param("sex", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"));
    }

}

