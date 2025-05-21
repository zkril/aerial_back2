package com.zkril.aerial_back;

import com.zkril.aerial_back.mapper.ProductCommentsMapper;
import com.zkril.aerial_back.mapper.UsersMapper;
import com.zkril.aerial_back.mapper.UserFavoriteMapper;
import com.zkril.aerial_back.pojo.ProductComments;
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
import java.util.Optional;

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
class ProductCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCommentsMapper productCommentsMapper;
    @MockBean
    private UsersMapper usersMapper;
    @MockBean
    private UserFavoriteMapper userFavoriteMapper;

    @Test
    void addComment_withoutPhoto_success() throws Exception {
        String content = "Test product comment";
        int userId = 10;
        int productId = 1000;
        when(productCommentsMapper.insert(any(ProductComments.class))).thenReturn(1);

        mockMvc.perform(multipart("/productComment/add")
                        .param("content", content)
                        .param("userId", String.valueOf(userId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        ArgumentCaptor<ProductComments> captor = ArgumentCaptor.forClass(ProductComments.class);
        verify(productCommentsMapper).insert(captor.capture());
        ProductComments savedComment = captor.getValue();
        assertEquals(content, savedComment.getIntro());
        assertEquals(userId, savedComment.getUserId());
        assertEquals(productId, savedComment.getProductId());
        assertEquals(0, savedComment.getIsread());
        assertEquals(0, savedComment.getNotified());
        assertNotNull(savedComment.getCreatedTime());
        assertTrue(savedComment.getPhoto() == null || savedComment.getPhoto().isEmpty());
    }

    @Test
    void addComment_withPhoto_success() throws Exception {
        String content = "Photo comment product";
        int userId = 11;
        int productId = 1001;
        when(productCommentsMapper.insert(any(ProductComments.class))).thenReturn(1);
        MockMultipartFile file = new MockMultipartFile("photo", "img.jpg", "image/jpeg", "dummy data".getBytes());

        mockMvc.perform(multipart("/productComment/add").file(file)
                        .param("content", content)
                        .param("userId", String.valueOf(userId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        ArgumentCaptor<ProductComments> captor = ArgumentCaptor.forClass(ProductComments.class);
        verify(productCommentsMapper).insert(captor.capture());
        ProductComments savedComment = captor.getValue();
        assertEquals(content, savedComment.getIntro());
        assertEquals(userId, savedComment.getUserId());
        assertEquals(productId, savedComment.getProductId());
        assertNotNull(savedComment.getPhoto());
        assertTrue(savedComment.getPhoto().startsWith("/comment-image/"));
        assertTrue(savedComment.getPhoto().endsWith(".jpg"));
    }

    @Test
    void getComments_returnsList_success() throws Exception {
        int productId = 500;
        int currentUserId = 15;
        // 准备评论列表
        ProductComments comment1 = new ProductComments();
        comment1.setId(101);
        comment1.setProductId(productId);
        comment1.setUserId(21);
        comment1.setIntro("First product comment");
        comment1.setLikeNumber(5);
        comment1.setLikedUserIds("15,30");  // 包含用户15
        comment1.setPhoto("pic1.png");
        comment1.setCreatedTime(LocalDateTime.now().minusDays(1));

        ProductComments comment2 = new ProductComments();
        comment2.setId(102);
        comment2.setProductId(productId);
        comment2.setUserId(22);
        comment2.setIntro("Second product comment");
        comment2.setLikeNumber(0);
        comment2.setLikedUserIds("");      // 无用户点赞
        comment2.setPhoto("");            // 无照片
        comment2.setCreatedTime(LocalDateTime.now());

        List<ProductComments> commentsList = Arrays.asList(comment2, comment1);
        when(productCommentsMapper.selectList(any())).thenReturn(commentsList);
        // 准备评论用户对应的用户信息
        Users user21 = new Users();
        user21.setUsername("User21");
        user21.setAvatar("/avatar21.png");
        Users user22 = new Users();
        user22.setUsername("User22");
        user22.setAvatar("/avatar22.png");
        when(usersMapper.selectById(21)).thenReturn(user21);
        when(usersMapper.selectById(22)).thenReturn(user22);

        mockMvc.perform(get("/productComment/list/{productId}", productId)
                        .param("userId", String.valueOf(currentUserId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                // 校验第一条评论（应为 comment2）
                .andExpect(jsonPath("$.data[0].id").value(102))
                .andExpect(jsonPath("$.data[0].name").value("User22"))
                .andExpect(jsonPath("$.data[0].avatar").value("http://localhost/" + user22.getAvatar()))
                .andExpect(jsonPath("$.data[0].intro").value("Second product comment"))
                .andExpect(jsonPath("$.data[0].likeNumber").value(0))
                .andExpect(jsonPath("$.data[0].photo").value(Matchers.nullValue()))
                .andExpect(jsonPath("$.data[0].liked").value(false))
                // 校验第二条评论（应为 comment1）
                .andExpect(jsonPath("$.data[1].id").value(101))
                .andExpect(jsonPath("$.data[1].name").value("User21"))
                .andExpect(jsonPath("$.data[1].avatar").value("http://localhost/" + user21.getAvatar()))
                .andExpect(jsonPath("$.data[1].intro").value("First product comment"))
                .andExpect(jsonPath("$.data[1].likeNumber").value(5))
                .andExpect(jsonPath("$.data[1].photo").value("http://localhost/" + comment1.getPhoto()))
                .andExpect(jsonPath("$.data[1].liked").value(true));

        verify(productCommentsMapper).selectList(any());
        verify(usersMapper).selectById(21);
        verify(usersMapper).selectById(22);
    }

    @Test
    void likeComment_addLike_success() throws Exception {
        int commentId = 201;
        int userId = 15;
        // 模拟一条用户15未点赞的评论
        ProductComments comment = new ProductComments();
        comment.setId(commentId);
        comment.setUserId(100);
        comment.setLikedUserIds("8,9");
        comment.setLikeNumber(1);
        when(productCommentsMapper.selectById(commentId)).thenReturn(comment);
        when(productCommentsMapper.updateById(any(ProductComments.class))).thenReturn(1);

        mockMvc.perform(post("/productComment/like")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证 updateById 调用后数据更新
        ArgumentCaptor<ProductComments> captor = ArgumentCaptor.forClass(ProductComments.class);
        verify(productCommentsMapper).updateById(captor.capture());
        ProductComments updated = captor.getValue();
        assertEquals(commentId, updated.getId());
        assertEquals(2, updated.getLikeNumber()); // likeNumber 从1增至2
        List<String> likedUsers = Arrays.asList(updated.getLikedUserIds().split(","));
        assertTrue(likedUsers.contains(String.valueOf(userId)));
    }

    @Test
    void likeComment_commentNotFound_fail() throws Exception {
        int commentId = 9999;
        int userId = 15;
        when(productCommentsMapper.selectById(commentId)).thenReturn(null);

        mockMvc.perform(post("/productComment/like")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value( "评论不存在"));

        verify(productCommentsMapper, never()).updateById(any());
    }

    @Test
    void likeComment_alreadyLiked_noChange() throws Exception {
        int commentId = 202;
        int userId = 15;
        // 模拟用户15已点赞的评论
        ProductComments comment = new ProductComments();
        comment.setId(commentId);
        comment.setLikedUserIds("15,16");
        comment.setLikeNumber(4);
        when(productCommentsMapper.selectById(commentId)).thenReturn(comment);

        mockMvc.perform(post("/productComment/like")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productCommentsMapper, never()).updateById(any());
        assertEquals(4, comment.getLikeNumber());
        assertTrue(comment.getLikedUserIds().contains("15"));
    }

    @Test
    void unlikeComment_removeLike_success() throws Exception {
        int commentId = 301;
        int userId = 15;
        // 模拟用户15已点赞，可取消点赞的评论
        ProductComments comment = new ProductComments();
        comment.setId(commentId);
        comment.setLikedUserIds("15,20");
        comment.setLikeNumber(2);
        when(productCommentsMapper.selectById(commentId)).thenReturn(comment);
        when(productCommentsMapper.updateById(any(ProductComments.class))).thenReturn(1);

        mockMvc.perform(post("/productComment/unlike")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        // 验证 updateById 调用后数据更新
        ArgumentCaptor<ProductComments> captor = ArgumentCaptor.forClass(ProductComments.class);
        verify(productCommentsMapper).updateById(captor.capture());
        ProductComments updated = captor.getValue();
        assertEquals(1, updated.getLikeNumber()); // likeNumber 从2减至1
        List<String> likedUsers = Arrays.asList(updated.getLikedUserIds().split(","));
        assertFalse(likedUsers.contains(String.valueOf(userId)));
    }

    @Test
    void unlikeComment_commentNotFound_fail() throws Exception {
        int commentId = 9998;
        int userId = 15;
        when(productCommentsMapper.selectById(commentId)).thenReturn(null);

        mockMvc.perform(post("/productComment/unlike")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("评论不存在"));

        verify(productCommentsMapper, never()).updateById(any());
    }

    @Test
    void unlikeComment_notLiked_noChange() throws Exception {
        int commentId = 302;
        int userId = 15;
        // 模拟用户15未点赞的评论
        ProductComments comment = new ProductComments();
        comment.setId(commentId);
        comment.setLikedUserIds("20,21");
        comment.setLikeNumber(3);
        when(productCommentsMapper.selectById(commentId)).thenReturn(comment);

        mockMvc.perform(post("/productComment/unlike")
                        .param("commentId", String.valueOf(commentId))
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(productCommentsMapper, never()).updateById(any());
        assertEquals(3, comment.getLikeNumber());
        assertFalse(comment.getLikedUserIds().contains(String.valueOf(userId)));
    }

    @Test
    void replyComment_success() throws Exception {
        String content = "Reply content product";
        int userId = 16;
        int parentCommentId = 101;
        when(productCommentsMapper.insert(any(ProductComments.class))).thenReturn(1);

        mockMvc.perform(post("/productComment/reply")
                        .param("content", content)
                        .param("userId", String.valueOf(userId))
                        .param("commentId", String.valueOf(parentCommentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        ArgumentCaptor<ProductComments> captor = ArgumentCaptor.forClass(ProductComments.class);
        verify(productCommentsMapper).insert(captor.capture());
        ProductComments savedReply = captor.getValue();
        assertEquals(content, savedReply.getIntro());
        assertEquals(userId, savedReply.getUserId());
        assertEquals(parentCommentId, savedReply.getReplyTo());
        assertEquals(0, savedReply.getIsread());
        assertEquals(0, savedReply.getNotified());
        assertNotNull(savedReply.getCreatedTime());
    }

    @Test
    void markRead_commentExists_success() throws Exception {
        Integer commentId = 401;
        ProductComments comment = new ProductComments();
        comment.setId((int) commentId);
        comment.setIsread(0);
        when(productCommentsMapper.selectById(commentId)).thenReturn(comment);
        when(productCommentsMapper.updateById(any(ProductComments.class))).thenReturn(1);

        mockMvc.perform(post("/productComment/read/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

    }

    @Test
    void markRead_commentNotFound_stillSuccess() throws Exception {
        long commentId = 402;
        when(productCommentsMapper.selectById(commentId)).thenReturn(null);

        mockMvc.perform(post("/productComment/read/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").value(Matchers.nullValue()));

        verify(productCommentsMapper, never()).updateById(any());
    }
}

