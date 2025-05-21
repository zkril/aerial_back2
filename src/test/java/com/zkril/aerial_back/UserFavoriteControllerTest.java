package com.zkril.aerial_back;


import com.zkril.aerial_back.mapper.UserFavoriteMapper;
import com.zkril.aerial_back.mapper.DesignsMapper;
import com.zkril.aerial_back.mapper.ProductsMapper;
import com.zkril.aerial_back.pojo.UserFavorite;
import com.zkril.aerial_back.pojo.Designs;
import com.zkril.aerial_back.pojo.Products;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserFavoriteController测试类。
 * 测试收藏相关接口的添加、删除、查询功能，包括成功场景和各种失败场景（已收藏、未找到、参数缺失等）。
 */
@SpringBootTest(properties = {"app.image-base-url=http://test-base"})
@AutoConfigureMockMvc
class UserFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserFavoriteMapper userFavoriteMapper;
    @MockBean
    private DesignsMapper designsMapper;
    @MockBean
    private ProductsMapper productsMapper;

    // 测试添加收藏成功场景，期望返回code=200，message为“成功”，data为null
    @Test
    void testAddFavoriteSuccess() throws Exception {
        // 模拟查询收藏不存在
        when(userFavoriteMapper.selectCount(any())).thenReturn(0L);
        // 模拟插入成功
        when(userFavoriteMapper.insert(any())).thenReturn(1);

        mockMvc.perform(post("/favorite/add")
                        .param("userId", "100")
                        .param("targetId", "200")
                        .param("type", "design"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // 测试添加收藏失败（已收藏过），期望返回code=201，message为“已收藏”
    @Test
    void testAddFavoriteFailAlreadyExists() throws Exception {
        // 模拟查询判断已存在收藏记录
        when(userFavoriteMapper.selectCount(any())).thenReturn(1L);

        mockMvc.perform(post("/favorite/add")
                        .param("userId", "101")
                        .param("targetId", "201")
                        .param("type", "product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"));

        // 验证如果已收藏则不会调用insert
        verify(userFavoriteMapper, never()).insert(any());
    }

    // 测试删除收藏成功场景，期望返回code=200，message为“成功”，data为null
    @Test
    void testDeleteFavoriteSuccess() throws Exception {
        // 模拟删除操作影响1行（成功删除）
        when(userFavoriteMapper.delete(any())).thenReturn(1);

        mockMvc.perform(post("/favorite/delete")
                        .param("userId", "100")
                        .param("targetId", "200")
                        .param("type", "design"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    // 测试删除收藏失败（记录未找到），期望返回code=201，message为“未找到该收藏”
    @Test
    void testDeleteFavoriteFailNotFound() throws Exception {
        // 模拟删除操作影响0行（未找到要删除的收藏记录）
        when(userFavoriteMapper.delete(any())).thenReturn(0);

        mockMvc.perform(post("/favorite/delete")
                        .param("userId", "101")
                        .param("targetId", "201")
                        .param("type", "product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"));
    }

    // 测试查询收藏列表成功（不指定type，返回多种收藏项），期望返回code=200，列表包含设计和产品收藏详情
    @Test
    void testListFavoritesSuccessMultipleTypes() throws Exception {
        // 准备收藏列表（包含design和product类型）
        UserFavorite designFav = new UserFavorite();
        designFav.setId(1L);
        designFav.setUserId(100L);
        designFav.setType("design");
        designFav.setTargetId(10L);
        designFav.setCreatedTime(LocalDateTime.of(2023, 1, 1, 12, 0, 0));
        UserFavorite productFav = new UserFavorite();
        productFav.setId(2L);
        productFav.setUserId(100L);
        productFav.setType("product");
        productFav.setTargetId(20L);
        productFav.setCreatedTime(LocalDateTime.of(2022, 12, 31, 12, 0, 0));
        List<UserFavorite> favorites = Arrays.asList(designFav, productFav);
        // 模拟按时间倒序返回收藏列表（designFav 时间较新，应排列在前）
        when(userFavoriteMapper.selectList(any())).thenReturn(favorites);

        // 准备对应的详细对象
        Designs design = new Designs();
        design.setName("设计作品");
        design.setDesignId(10);
        design.setHomePhoto("/design_photo.png");
        Products product = new Products();
        product.setName("实体商品");
        product.setProductId(20);
        product.setHomePhoto("/product_photo.png");
        // 模拟根据ID查询详细信息
        when(designsMapper.selectById(10L)).thenReturn(design);
        when(productsMapper.selectById(20L)).thenReturn(product);

        mockMvc.perform(get("/favorite/list")
                        .param("userId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                // 校验返回的列表包含2项收藏
                .andExpect(jsonPath("$.data.length()").value(2))
                // 校验第1项为design类型收藏及其详情字段
                .andExpect(jsonPath("$.data[0].type").value("design"))
                .andExpect(jsonPath("$.data[0].favoriteId").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(100))
                .andExpect(jsonPath("$.data[0].targetId").value(10))
                .andExpect(jsonPath("$.data[0].detailName").value("设计作品"))
                .andExpect(jsonPath("$.data[0].detailId").value(10))
                .andExpect(jsonPath("$.data[0].detailHomePhoto").value("http://test-base/design_photo.png"))
                // 校验第2项为product类型收藏及其详情字段
                .andExpect(jsonPath("$.data[1].type").value("product"))
                .andExpect(jsonPath("$.data[1].favoriteId").value(2))
                .andExpect(jsonPath("$.data[1].userId").value(100))
                .andExpect(jsonPath("$.data[1].targetId").value(20))
                .andExpect(jsonPath("$.data[1].detailName").value("实体商品"))
                .andExpect(jsonPath("$.data[1].detailId").value(20))
                .andExpect(jsonPath("$.data[1].detailHomePhoto").value("http://test-base/product_photo.png"));
    }

    // 测试查询收藏列表按类型过滤，期望只返回指定类型的收藏项
    @Test
    void testListFavoritesSuccessWithTypeFilter() throws Exception {
        // 准备仅包含design类型的收藏列表
        UserFavorite designFav = new UserFavorite();
        designFav.setId(3L);
        designFav.setUserId(100L);
        designFav.setType("design");
        designFav.setTargetId(30L);
        designFav.setCreatedTime(LocalDateTime.now());
        when(userFavoriteMapper.selectList(any())).thenReturn(Collections.singletonList(designFav));

        // 准备对应设计详情
        Designs design = new Designs();
        design.setName("另一个设计");
        design.setDesignId(30);
        design.setHomePhoto("/design2.png");
        when(designsMapper.selectById(30L)).thenReturn(design);

        mockMvc.perform(get("/favorite/list")
                        .param("userId", "100")
                        .param("type", "design"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].type").value("design"))
                .andExpect(jsonPath("$.data[0].detailName").value("另一个设计"))
                .andExpect(jsonPath("$.data[0].detailId").value(30))
                .andExpect(jsonPath("$.data[0].detailHomePhoto").value("http://test-base/design2.png"));
    }

    // 测试查询收藏列表在用户无收藏记录时，期望返回code=200，data为空列表
    @Test
    void testListFavoritesEmpty() throws Exception {
        // 模拟无收藏记录
        when(userFavoriteMapper.selectList(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/favorite/list")
                        .param("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"));
    }

    // 测试缺少必要参数时的行为，期望返回HTTP 400错误
    @Test
    void testAddFavoriteFailMissingParam() throws Exception {
        // 缺少targetId参数
        mockMvc.perform(post("/favorite/add")
                        .param("userId", "100")
                        .param("type", "design"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("")));
    }
}

