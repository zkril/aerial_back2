package com.zkril.aerial_back;

import com.zkril.aerial_back.mapper.UserSessionsMapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.mapper.FoldersMapper;
import com.zkril.aerial_back.pojo.Folders;
import com.zkril.aerial_back.pojo.UserSessions;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.service.MailService;
import com.zkril.aerial_back.util.VerifyCodeCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.image-base-url=http://test.server/")
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersMapper usersMapper;
    @MockBean
    private FoldersMapper foldersMapper;
    @MockBean
    private UserSessionsMapper sessionsMapper;
    @MockBean
    private VerifyCodeCache cache;
    @MockBean
    private MailService mailService;
    @Autowired
    private UserSessionsMapper userSessionsMapper;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test to avoid interference
        reset(usersMapper, foldersMapper, cache, mailService,sessionsMapper);
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Arrange: prepare a Users record and a Folders record for successful login
        String loginEmail = "test@example.com";
        String loginPassword = "pass123";
        Users user = new Users();
        user.setUserId(1);
        user.setUsername("testUser");
        user.setEmail(loginEmail);
        user.setPassword(loginPassword);
        user.setAvatar("default-avatar.png");  // stored avatar path
        Folders defaultFolder = new Folders();
        defaultFolder.setId(100);
        // Stub the usersMapper and foldersMapper calls
        when(usersMapper.selectOne(any())).thenReturn(user);
        when(foldersMapper.selectOne(any())).thenReturn(defaultFolder);

        // Act: perform the login request with correct credentials
        mockMvc.perform(post("http://test.server/login")
                        .param("userName", loginEmail)
                        .param("passwordHash", loginPassword))
                // Assert: verify HTTP 200 and JSON response fields
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("testUser"))
                .andExpect(jsonPath("$.data.user.email").value(loginEmail))
                .andExpect(jsonPath("$.data.user.avatar").value("http://test.server/default-avatar.png"))
                .andExpect(jsonPath("$.data.user.password").value(nullValue()))
                .andExpect(jsonPath("$.data.folder").value(100))
                .andReturn();

        // Verify: usersMapper was called to fetch user, and foldersMapper was called for default folder
        verify(usersMapper, times(1)).selectOne(any());
        verify(foldersMapper, times(1)).selectOne(any());
        // The user object password should be cleared (null or empty) after login
        assertTrue(user.getPassword() == null || user.getPassword().isEmpty());
        // The avatar URL should be prefixed with baseURL
        assertTrue(user.getAvatar().startsWith("http://test.server/"));
        // No verification code or email sending should occur during login
        verifyNoInteractions(cache);
        verifyNoInteractions(mailService);
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        // Arrange: no user exists for the given email
        String loginEmail = "nouser@example.com";
        when(usersMapper.selectOne(any())).thenReturn(null);

        // Act & Assert: perform login and expect failure response for non-existent user
        mockMvc.perform(post("/login")
                        .param("userName", loginEmail)
                        .param("passwordHash", "anyPass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: user lookup was attempted, but no folder lookup or other actions should occur
        verify(usersMapper, times(1)).selectOne(any());
        verify(foldersMapper, never()).selectOne(any());
        verifyNoInteractions(cache);
        verifyNoInteractions(mailService);
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        // Arrange: user exists but password does not match
        Users user = new Users();
        user.setUserId(2);
        user.setEmail("user2@example.com");
        user.setUsername("user2");
        user.setPassword("correctHash");
        when(usersMapper.selectOne(any())).thenReturn(user);

        // Act & Assert: perform login with wrong password and expect failure response
        mockMvc.perform(post("/login")
                        .param("userName", "user2@example.com")
                        .param("passwordHash", "wrongHash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: user lookup was done, but no folder lookup or token generation on wrong password
        verify(usersMapper, times(1)).selectOne(any());
        verify(foldersMapper, never()).selectOne(any());
        verifyNoInteractions(cache);
        verifyNoInteractions(mailService);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange: 设置测试数据
        String userName = "newUser";
        String email = "newuser@example.com";
        String password = "NewPass123";
        String confirmPassword = "NewPass123";
        String verifyCode = "123456";

        // 模拟验证码校验成功
        when(cache.verify(email, verifyCode)).thenReturn(true);

        // 模拟用户名和邮箱均未被注册
        when(usersMapper.selectOne(argThat(wrapper -> wrapper.getSqlSegment().contains("userName")))).thenReturn(null);

        // 模拟用户插入成功并返回生成的userId
        doAnswer(invocation -> {
            Users userArg = invocation.getArgument(0);
            userArg.setUserId(100);  // 模拟数据库生成userId
            return 1;
        }).when(usersMapper).insert(org.mockito.ArgumentMatchers.any(Users.class));

        // 模拟默认文件夹插入成功
        when(foldersMapper.insert(org.mockito.ArgumentMatchers.any(Folders.class))).thenReturn(1);

        // 模拟UserSessions插入成功
        when(sessionsMapper.insert(org.mockito.ArgumentMatchers.any(UserSessions.class))).thenReturn(1);

        // Act: 执行注册请求
        mockMvc.perform(post("/register")
                        .param("userName", userName)
                        .param("email", email)
                        .param("passwordHash", password)
                        .param("confirmPassword", confirmPassword)
                        .param("code", verifyCode))
                // Assert: 验证响应成功
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("注册成功"));

        // Verify: 校验验证码被调用且注册成功后删除
        verify(cache, times(1)).verify(email, verifyCode);
        verify(cache, times(1)).remove(email);

        // Verify: 用户被正确插入
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(usersMapper, times(1)).insert(userCaptor.capture());
        Users insertedUser = userCaptor.getValue();
        assertEquals(userName, insertedUser.getUsername());
        assertEquals(email, insertedUser.getEmail());
        assertEquals(password, insertedUser.getPassword());

        // Verify: 默认文件夹被创建
        ArgumentCaptor<Folders> folderCaptor = ArgumentCaptor.forClass(Folders.class);
        verify(foldersMapper, times(1)).insert(folderCaptor.capture());
        Folders createdFolder = folderCaptor.getValue();
        assertEquals(100, createdFolder.getUserId());
        assertEquals("默认文件夹", createdFolder.getName());

        // Verify: UserSessions插入成功
        ArgumentCaptor<UserSessions> sessionsCaptor = ArgumentCaptor.forClass(UserSessions.class);
        verify(userSessionsMapper, times(1)).insert(sessionsCaptor.capture());
        UserSessions createdSession = sessionsCaptor.getValue();
        assertEquals(1, createdSession.getUser1Id());
        assertEquals(100, createdSession.getUser2Id());
    }

    @Test
    void testRegisterMissingFields() throws Exception {
        // Arrange: prepare a request with a missing (empty) required field
        String email = "user@example.com";
        String password = "Password1";
        // We will leave userName empty to simulate missing field
        // Act & Assert: perform register and expect missing field error
        mockMvc.perform(post("/register")
                        .param("userName", "")
                        .param("email", email)
                        .param("passwordHash", password)
                        .param("confirmPassword", password)
                        .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: no verification or database actions should occur due to early validation failure
        verifyNoInteractions(usersMapper);
        verifyNoInteractions(foldersMapper);
        verifyNoInteractions(cache);
        verifyNoInteractions(mailService);
    }

    @Test
    void testRegisterConfirmPasswordMismatch() throws Exception {
        // Arrange: prepare data where password and confirmPassword do not match
        String userName = "userX";
        String email = "userx@example.com";
        String password = "ABCdef12";
        String confirmPassword = "XYZdef12";
        // Act & Assert: expect failure due to password confirmation mismatch
        mockMvc.perform(post("/register")
                        .param("userName", userName)
                        .param("email", email)
                        .param("passwordHash", password)
                        .param("confirmPassword", confirmPassword)
                        .param("code", "654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: should return before checking code or any database operations
        verifyNoInteractions(usersMapper);
        verifyNoInteractions(foldersMapper);
        verifyNoInteractions(cache);
        verifyNoInteractions(mailService);
    }

    @Test
    void testRegisterInvalidUsernameFormat() throws Exception {
        // Arrange: userName with invalid format (too short)
        String shortName = "a";
        // Provide other fields valid
        String email = "format@example.com";
        String password = "ValidPass1";
        // Stub code verify to true so that format check is reached
        when(cache.verify(email, "123456")).thenReturn(true);

        // Act & Assert: perform register with invalid username format
        mockMvc.perform(post("/register")
                        .param("userName", shortName)
                        .param("email", email)
                        .param("passwordHash", password)
                        .param("confirmPassword", password)
                        .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: no user insertion or folder creation should happen due to validation failure
        verify(cache, times(1)).verify(email, "123456");
        verify(usersMapper, never()).insert(any());
        verify(foldersMapper, never()).insert(any());
    }

    @Test
    void testRegisterVerificationCodeInvalid() throws Exception {
        // Arrange: prepare valid inputs but an incorrect verification code
        String email = "temp@example.com";
        when(cache.verify(email, "000000")).thenReturn(false);

        // Act & Assert: attempt register with wrong code
        mockMvc.perform(post("/register")
                        .param("userName", "tempUser")
                        .param("email", email)
                        .param("passwordHash", "SomePass1")
                        .param("confirmPassword", "SomePass1")
                        .param("code", "000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: code verification was attempted, but no user creation on failure
        verify(cache, times(1)).verify(email, "000000");
        verify(usersMapper, never()).insert(any());
        verify(foldersMapper, never()).insert(any());
        // The verification code should not be removed on failure
        verify(cache, never()).remove(anyString());
    }

    @Test
    void testRegisterUsernameAlreadyExists() throws Exception {
        // Arrange: simulate existing user with the same username
        String userName = "duplicate";
        String email = "new@example.com";
        // First usersMapper.selectOne call (for username) returns a user (exists), second call not used due to early return
        Users existingUser = new Users();
        existingUser.setUsername(userName);
        when(usersMapper.selectOne(any())).thenReturn(existingUser);

        // Act & Assert: attempt register with a username that already exists
        mockMvc.perform(post("/register")
                        .param("userName", userName)
                        .param("email", email)
                        .param("passwordHash", "Pass12345")
                        .param("confirmPassword", "Pass12345")
                        .param("code", "111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: checked username existence and returned failure, no further actions
        verify(usersMapper, never()).insert(any());
        verify(foldersMapper, never()).insert(any());
        verify(cache, never()).remove(anyString());
    }

    @Test
    void testRegisterEmailAlreadyExists() throws Exception {
        // Arrange: simulate existing user with the same email
        String userName = "uniqueName";
        String email = "duplicate@example.com";
        // First call (username check) returns null (no conflict), second call (email check) returns a user to simulate conflict
        when(usersMapper.selectOne(any()))
                .thenReturn(null)
                .thenReturn(new Users());

        // Act & Assert: attempt register with an email that already exists
        mockMvc.perform(post("/register")
                        .param("userName", userName)
                        .param("email", email)
                        .param("passwordHash", "SomePass9")
                        .param("confirmPassword", "SomePass9")
                        .param("code", "222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: both username and email checks were performed, and failure returned on email conflict
        verify(usersMapper, never()).insert(any());
        verify(foldersMapper, never()).insert(any());
        verify(cache, never()).remove(anyString());
    }

    @Test
    void testSendCodeSuccess() throws Exception {
        // Arrange: use a valid email for sending verification code
        String email = "testsend@example.com";

        // Act: perform send_code request
        mockMvc.perform(post("/send_code")
                        .param("email", email))
                // Assert: expect success status and message
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andReturn();

        // Capture the verification code saved in cache and sent via email
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(cache, times(1)).save(eq(email), codeCaptor.capture());
        String sentCode = codeCaptor.getValue();
        verify(mailService, times(1)).sendCode(eq(email), eq(sentCode));
        // Assert that the code is a 6-digit number
        assertNotNull(sentCode);
        assertTrue(sentCode.matches("\\d{6}"));
        // Verify: no user database lookup should be performed for normal send_code
        verifyNoInteractions(usersMapper);
        verifyNoInteractions(foldersMapper);
    }

    @Test
    void testSendCodeInvalidEmail() throws Exception {
        // Arrange: an invalid email format
        String badEmail = "invalid-email";
        // Act & Assert: perform send_code with invalid email and expect failure
        mockMvc.perform(post("/send_code")
                        .param("email", badEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: no code should be generated or sent due to validation failure
        verify(cache, never()).save(anyString(), anyString());
        verify(mailService, never()).sendCode(anyString(), anyString());
    }

    @Test
    void testForgotSendCodeSuccess() throws Exception {
        // Arrange: use an email that is registered
        String email = "exists@example.com";
        when(usersMapper.selectOne(any())).thenReturn(new Users());  // simulate existing user

        // Act: perform forgot/send_code request with valid and existing email
        mockMvc.perform(post("/forgot/send_code")
                        .param("email", email))
                // Assert: expect success response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("验证码已发送"))
                .andReturn();

        // Capture and verify the code generation and email sending
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(cache, times(1)).save(eq(email), codeCaptor.capture());
        String sentCode = codeCaptor.getValue();
        verify(mailService, times(1)).sendCode(eq(email), eq(sentCode));
        assertTrue(sentCode.matches("\\d{6}"));
        // Verify: user existence was checked once
        verify(usersMapper, times(1)).selectOne(any());
    }

    @Test
    void testForgotSendCodeEmailNotRegistered() throws Exception {
        // Arrange: simulate no user with given email
        String email = "notregistered@example.com";
        when(usersMapper.selectOne(any())).thenReturn(null);

        // Act & Assert: request forgot/send_code with unregistered email
        mockMvc.perform(post("/forgot/send_code")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.data").value("该邮箱未注册"))
                .andReturn();

        // Verify: user lookup was performed, but no code sent
        verify(usersMapper, times(1)).selectOne(any());
        verify(cache, never()).save(anyString(), anyString());
        verify(mailService, never()).sendCode(anyString(), anyString());
    }

    @Test
    void testForgotSendCodeInvalidEmail() throws Exception {
        // Arrange: invalid email format
        String email = "badformat.com";
        // Act & Assert: attempt to send code with invalid email
        mockMvc.perform(post("/forgot/send_code")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: no code generation or email sending due to validation failure
        verify(usersMapper, never()).selectOne(any());
        verify(cache, never()).save(anyString(), anyString());
        verify(mailService, never()).sendCode(anyString(), anyString());
    }

    @Test
    void testResetPasswordSuccess() throws Exception {
        // Arrange: prepare a user and stub dependencies for successful password reset
        String email = "userreset@example.com";
        String correctCode = "888888";
        String newPassword = "NewPassword1";
        // Simulate cache verify returns true for the code
        when(cache.verify(email, correctCode)).thenReturn(true);
        // Simulate an existing user record in DB
        Users user = new Users();
        user.setUserId(5);
        user.setEmail(email);
        user.setPassword("oldPassword");
        when(usersMapper.selectOne(any())).thenReturn(user);
        // Simulate successful DB update
        when(usersMapper.updateById(org.mockito.ArgumentMatchers.any(Users.class))).thenReturn(1);

        // Act: perform the reset_password request with valid code and password
        mockMvc.perform(post("/forgot/reset_password")
                        .param("email", email)
                        .param("code", correctCode)
                        .param("newPassword", newPassword))
                // Assert: expect success result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("密码重置成功"))
                .andReturn();

        // Verify: verification code was checked and then removed
        verify(cache, times(1)).verify(email, correctCode);
        verify(cache, times(1)).remove(email);
        // Verify: password was updated in the database
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(usersMapper).updateById(userCaptor.capture());
        Users updatedUser = userCaptor.getValue();
        assertEquals(newPassword, updatedUser.getPassword());
    }

    @Test
    void testResetPasswordInvalidCode() throws Exception {
        // Arrange: prepare input with wrong verification code
        String email = "abc@example.com";
        when(cache.verify(email, "999999")).thenReturn(false);

        // Act & Assert: attempt password reset with incorrect code
        mockMvc.perform(post("/forgot/reset_password")
                        .param("email", email)
                        .param("code", "999999")
                        .param("newPassword", "Whatever123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.data").value("验证码无效或已过期"))
                .andReturn();

        // Verify: code verification was attempted, but password not updated or code removed
        verify(cache, times(1)).verify(email, "999999");
        verify(usersMapper, never()).updateById(any());
        verify(cache, never()).remove(anyString());
    }

    @Test
    void testResetPasswordMissingParam() throws Exception {
        // Arrange: leave newPassword empty to simulate missing parameter
        String email = "abc@example.com";
        String code = "123123";
        // Act & Assert: perform reset_password with a missing field
        mockMvc.perform(post("/forgot/reset_password")
                        .param("email", email)
                        .param("code", code)
                        .param("newPassword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: no verification or update should occur due to missing parameter
        verify(cache, never()).verify(anyString(), anyString());
        verify(usersMapper, never()).selectOne(any());
        verify(usersMapper, never()).updateById(any());
        verify(cache, never()).remove(anyString());
    }

    @Test
    void testResetPasswordInvalidPasswordFormat() throws Exception {
        // Arrange: prepare a new password that does not meet format requirements (too short)
        String email = "format@example.com";
        String code = "333333";
        String badPassword = "123"; // too short (less than 6 chars)
        when(cache.verify(email, code)).thenReturn(true);

        // Act & Assert: attempt reset_password with invalid new password format
        mockMvc.perform(post("/forgot/reset_password")
                        .param("email", email)
                        .param("code", code)
                        .param("newPassword", badPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: code was verified, but no update performed due to format failure
        verify(cache, times(1)).verify(email, code);
        verify(usersMapper, never()).updateById(any());
        verify(cache, never()).remove(anyString());
    }

    @Test
    void testResetPasswordUserNotFound() throws Exception {
        // Arrange: simulate scenario where email is not associated with any user
        String email = "nouser@example.com";
        String code = "444444";
        String newPassword = "ValidPass9";
        when(cache.verify(email, code)).thenReturn(true);
        // Stub user lookup to return null (user not found)
        when(usersMapper.selectOne(any())).thenReturn(null);

        // Act & Assert: attempt reset_password for unregistered email
        mockMvc.perform(post("/forgot/reset_password")
                        .param("email", email)
                        .param("code", code)
                        .param("newPassword", newPassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(equalTo(200))))
                .andExpect(jsonPath("$.message").value("失败"))
                .andReturn();

        // Verify: user lookup was performed after code verification, but no update due to missing user
        verify(cache, times(1)).verify(email, code);
        verify(usersMapper, times(1)).selectOne(any());
        verify(usersMapper, never()).updateById(any());
        verify(cache, never()).remove(anyString());
    }

}

