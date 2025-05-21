package com.zkril.aerial_back;


import com.zkril.aerial_back.mapper.DesignCommentsMapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.pojo.DesignComments;
import com.zkril.aerial_back.pojo.Users;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.image-base-url=http://localhost/",
        "app.comment-image=uploads/test/"
})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.image-base-url=http://localhost/",
        "app.comment-image=uploads/test/"
})
class DesignCommentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesignCommentsMapper designCommentsMapper;
    @MockBean
    private UsersMapper usersMapper;

    @Test
    void addComment_withoutPhoto_success() throws Exception {
        // 准备请求参数，无图片上传
        String content = "Test comment content";
        int userId = 1;
        int designId = 100;
        when(designCommentsMapper.insert(any(DesignComments.class))).thenReturn(1);

        // 执行请求（无文件）
        mockMvc.perform(multipart("/designComment/add")
                        .param("content", content)
                        .param("userId", String.valueOf(userId))
                        .param("designId", String.valueOf(designId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证 mapper.insert 调用并捕获保存的评论对象
        ArgumentCaptor<DesignComments> captor = ArgumentCaptor.forClass(DesignComments.class);
        verify(designCommentsMapper).insert(captor.capture());
        DesignComments savedComment = captor.getValue();
        // 验证保存的评论字段
        assertEquals(content, savedComment.getIntro());
        assertEquals(userId, savedComment.getUserId());
        assertEquals(designId, savedComment.getDesignId());
        assertEquals(0, savedComment.getIsread());
        assertEquals(0, savedComment.getNotified());
        assertNotNull(savedComment.getCreatedTime());
        // 无照片上传，photo 字段应为空
        assertTrue(savedComment.getPhoto() == null || savedComment.getPhoto().isEmpty());
    }

    @Test
    void addComment_withPhoto_success() throws Exception {
        // 准备请求参数，包含图片上传
        String content = "Photo comment";
        int userId = 2;
        int designId = 101;
        when(designCommentsMapper.insert(any(DesignComments.class))).thenReturn(1);
        // 模拟上传的图片文件
        MockMultipartFile file = new MockMultipartFile("photo", "test.jpg", "image/jpeg", "dummy image content".getBytes());

        mockMvc.perform(multipart("/designComment/add").file(file)
                        .param("content", content)
                        .param("userId", String.valueOf(userId))
                        .param("designId", String.valueOf(designId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证保存的评论对象包含正确的 photo 路径
        ArgumentCaptor<DesignComments> captor = ArgumentCaptor.forClass(DesignComments.class);
        verify(designCommentsMapper).insert(captor.capture());
        DesignComments savedComment = captor.getValue();
        assertEquals(content, savedComment.getIntro());
        assertEquals(userId, savedComment.getUserId());
        assertEquals(designId, savedComment.getDesignId());
        assertNotNull(savedComment.getPhoto());
        assertTrue(savedComment.getPhoto().startsWith("/comment-image/"));
        assertTrue(savedComment.getPhoto().endsWith(".jpg"));
    }

    @Test
    void getComments_returnsList_success() throws Exception {
        int designId = 200;
        int currentUserId = 5;
        // 准备两条评论数据
        DesignComments comment1 = new DesignComments();
        comment1.setId(1);
        comment1.setDesignId(designId);
        comment1.setUserId(11);
        comment1.setIntro("First comment");
        comment1.setLikeNumber(2);
        comment1.setLikedUserIds("5,7");  // 包含用户5，表示用户5已点赞
        comment1.setPhoto("image1.png");
        comment1.setCreatedTime(LocalDateTime.now().minusHours(1));

        DesignComments comment2 = new DesignComments();
        comment2.setId(2);
        comment2.setDesignId(designId);
        comment2.setUserId(12);
        comment2.setIntro("Second comment");
        comment2.setLikeNumber(0);
        comment2.setLikedUserIds("8,9");  // 不包含用户5
        comment2.setPhoto("");           // 无照片
        comment2.setCreatedTime(LocalDateTime.now());

        // 模拟 selectList 返回按时间倒序排列的列表（comment2 时间更新、更靠前）
        List<DesignComments> commentList = Arrays.asList(comment2, comment1);
        when(designCommentsMapper.selectList(any())).thenReturn(commentList);
        // 准备对应的用户信息
        Users user11 = new Users();
        user11.setUsername("User11");
        user11.setAvatar("/avatar11.png");
        Users user12 = new Users();
        user12.setUsername("User12");
        user12.setAvatar("/avatar12.png");
        when(usersMapper.selectById(11)).thenReturn(user11);
        when(usersMapper.selectById(12)).thenReturn(user12);

        mockMvc.perform(get("/designComment/list/{designId}", designId)
                        .param("userId", String.valueOf(currentUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                // 校验第一条评论（应为 comment2）
                .andExpect(jsonPath("$.data[0].id").value(2))
                .andExpect(jsonPath("$.data[0].name").value("User12"))
                .andExpect(jsonPath("$.data[0].avatar").value("http://localhost/" + user12.getAvatar()))
                .andExpect(jsonPath("$.data[0].intro").value("Second comment"))
                .andExpect(jsonPath("$.data[0].likeNumber").value(0))
                .andExpect(jsonPath("$.data[0].photo").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].liked").value(false))
                // 校验第二条评论（应为 comment1）
                .andExpect(jsonPath("$.data[1].id").value(1))
                .andExpect(jsonPath("$.data[1].name").value("User11"))
                .andExpect(jsonPath("$.data[1].avatar").value("http://localhost/" + user11.getAvatar()))
                .andExpect(jsonPath("$.data[1].intro").value("First comment"))
                .andExpect(jsonPath("$.data[1].likeNumber").value(2))
                .andExpect(jsonPath("$.data[1].photo").value("http://localhost/" + comment1.getPhoto()))
                .andExpect(jsonPath("$.data[1].liked").value(true));

        verify(designCommentsMapper).selectList(any());
        verify(usersMapper).selectById(11);
        verify(usersMapper).selectById(12);
    }

    @Test
    void likeComment_addLike_success() throws Exception {
        int commentId = 10;
        int userId = 5;
        // 模拟一条评论（用户5尚未点赞）
        DesignComments comment = new DesignComments();
        comment.setId(commentId);
        comment.setUserId(100);
        comment.setLikedUserIds("2,3");
        comment.setLikeNumber(2);
        when(designCommentsMapper.selectById(commentId)).thenReturn(comment);
        when(designCommentsMapper.updateById(any(DesignComments.class))).thenReturn(1);

        mockMvc.perform(post("/designComment/like")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证 updateById 调用，并检查点赞数据已更新
        ArgumentCaptor<DesignComments> captor = ArgumentCaptor.forClass(DesignComments.class);
        verify(designCommentsMapper).updateById(captor.capture());
        DesignComments updated = captor.getValue();
        assertEquals(commentId, updated.getId());
        // likeNumber 应加1
        assertEquals(3, updated.getLikeNumber());
        // likedUserIds 应包含刚刚点赞的用户ID
        List<String> likedUsers = Arrays.asList(updated.getLikedUserIds().split(","));
        assertTrue(likedUsers.contains(String.valueOf(userId)));
    }

    @Test
    void likeComment_commentNotFound_fail() throws Exception {
        int commentId = 999;
        int userId = 5;
        when(designCommentsMapper.selectById(commentId)).thenReturn(null);

        mockMvc.perform(post("/designComment/like")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())  // 控制器返回 200 状态
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("评论不存在"));

        verify(designCommentsMapper, never()).updateById(any());
    }

    @Test
    void likeComment_alreadyLiked_noChange() throws Exception {
        int commentId = 11;
        int userId = 5;
        // 模拟一条评论（用户5已点赞过）
        DesignComments comment = new DesignComments();
        comment.setId(commentId);
        comment.setLikedUserIds("1,5,9");
        comment.setLikeNumber(5);
        when(designCommentsMapper.selectById(commentId)).thenReturn(comment);

        mockMvc.perform(post("/designComment/like")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 已点赞情况下不应触发更新操作，数据不变
        verify(designCommentsMapper, never()).updateById(any());
        assertEquals(5, comment.getLikeNumber());
        assertTrue(comment.getLikedUserIds().contains("5"));
    }

    @Test
    void unlikeComment_removeLike_success() throws Exception {
        int commentId = 20;
        int userId = 5;
        // 模拟一条评论（用户5已点赞，可取消点赞）
        DesignComments comment = new DesignComments();
        comment.setId(commentId);
        comment.setLikedUserIds("5,7");
        comment.setLikeNumber(2);
        when(designCommentsMapper.selectById(commentId)).thenReturn(comment);
        when(designCommentsMapper.updateById(any(DesignComments.class))).thenReturn(1);

        mockMvc.perform(post("/designComment/unlike")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证 updateById 调用，并检查取消点赞后的数据
        ArgumentCaptor<DesignComments> captor = ArgumentCaptor.forClass(DesignComments.class);
        verify(designCommentsMapper).updateById(captor.capture());
        DesignComments updated = captor.getValue();
        assertEquals(1, updated.getLikeNumber());  // likeNumber 从2减少到1
        List<String> likedUsers = Arrays.asList(updated.getLikedUserIds().split(","));
        assertFalse(likedUsers.contains(String.valueOf(userId)));
    }

    @Test
    void unlikeComment_commentNotFound_fail() throws Exception {
        int commentId = 998;
        int userId = 5;
        when(designCommentsMapper.selectById(commentId)).thenReturn(null);

        mockMvc.perform(post("/designComment/unlike")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("评论不存在"));

        verify(designCommentsMapper, never()).updateById(any());
    }

    @Test
    void unlikeComment_notLiked_noChange() throws Exception {
        int commentId = 21;
        int userId = 5;
        // 模拟一条评论（用户5未点赞，无需操作）
        DesignComments comment = new DesignComments();
        comment.setId(commentId);
        comment.setLikedUserIds("7,8");
        comment.setLikeNumber(2);
        when(designCommentsMapper.selectById(commentId)).thenReturn(comment);

        mockMvc.perform(post("/designComment/unlike")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(designCommentsMapper, never()).updateById(any());
        assertEquals(2, comment.getLikeNumber());
        assertFalse(comment.getLikedUserIds().contains(String.valueOf(userId)));
    }

    @Test
    void replyComment_success() throws Exception {
        String content = "Reply content";
        int userId = 3;
        int parentCommentId = 1;
        when(designCommentsMapper.insert(any(DesignComments.class))).thenReturn(1);

        mockMvc.perform(post("/designComment/reply")
                        .param("content", content)
                        .param("userId", String.valueOf(userId))
                        .param("commentId", String.valueOf(parentCommentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证保存的回复评论
        ArgumentCaptor<DesignComments> captor = ArgumentCaptor.forClass(DesignComments.class);
        verify(designCommentsMapper).insert(captor.capture());
        DesignComments savedReply = captor.getValue();
        assertEquals(content, savedReply.getIntro());
        assertEquals(userId, savedReply.getUserId());
        assertEquals(parentCommentId, savedReply.getReplyTo());
        assertEquals(0, savedReply.getIsread());
        assertEquals(0, savedReply.getNotified());
        assertNotNull(savedReply.getCreatedTime());
    }

    @Test
    void markRead_commentExists_success() throws Exception {
        Integer commentId = 30;
        DesignComments comment = new DesignComments();
        comment.setId(commentId);
        comment.setIsread(0);
        when(designCommentsMapper.selectById(commentId)).thenReturn(comment);
        when(designCommentsMapper.updateById(any(DesignComments.class))).thenReturn(1);

        mockMvc.perform(post("/designComment/read/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists());

    }

    @Test
    void markRead_commentNotFound_stillSuccess() throws Exception {
        long commentId = 31L;
        when(designCommentsMapper.selectById(commentId)).thenReturn(null);

        mockMvc.perform(post("/designComment/read/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        verify(designCommentsMapper, never()).updateById(any());
    }
}


