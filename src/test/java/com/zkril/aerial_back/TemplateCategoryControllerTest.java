package com.zkril.aerial_back;


import com.zkril.aerial_back.mapper.TemplateCategoryMapper;
import com.zkril.aerial_back.pojo.TemplateCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TemplateCategoryController测试类。
 * 测试模板分类树接口，包括返回完整分类树的成功场景和无分类数据时的边界场景。
 */
@SpringBootTest
@AutoConfigureMockMvc
class TemplateCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TemplateCategoryMapper templateCategoryMapper;

    // 测试获取模板分类树成功，期望返回code=200，数据按树形结构组织正确
    @Test
    void testGetTemplateTreeSuccess() throws Exception {
        // 构造模板分类数据
        TemplateCategory root1 = new TemplateCategory();
        root1.setId(1);
        root1.setParentId(null);
        root1.setName("根分类1");
        root1.setTemplateType("type1");
        root1.setIcon(null);
        root1.setPhoto(null);
        TemplateCategory child11 = new TemplateCategory();
        child11.setId(2);
        child11.setParentId(1);
        child11.setName("子分类1-1");
        child11.setTemplateType("type1-1");
        child11.setIcon("icon1.png");
        child11.setPhoto(null);
        TemplateCategory child12 = new TemplateCategory();
        child12.setId(3);
        child12.setParentId(1);
        child12.setName("子分类1-2");
        child12.setTemplateType("type1-2");
        child12.setIcon(null);
        child12.setPhoto("photo2.png");
        TemplateCategory root2 = new TemplateCategory();
        root2.setId(4);
        root2.setParentId(null);
        root2.setName("根分类2");
        root2.setTemplateType("type2");
        root2.setIcon(null);
        root2.setPhoto(null);
        List<TemplateCategory> allCategories = Arrays.asList(root1, child11, child12, root2);
        when(templateCategoryMapper.selectList(null)).thenReturn(allCategories);

        mockMvc.perform(get("/templates/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                // 应返回两个根节点
                .andExpect(jsonPath("$.data.length()").value(2))
                // 校验第一个根分类节点及其子节点
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].label").value("根分类1"))
                .andExpect(jsonPath("$.data[0].name").value("type1"))
                .andExpect(jsonPath("$.data[0].children.length()").value(2))
                .andExpect(jsonPath("$.data[0].children[0].label").value("子分类1-1"))
                .andExpect(jsonPath("$.data[0].children[0].icon").value("icon1.png"))
                .andExpect(jsonPath("$.data[0].children[1].label").value("子分类1-2"))
                .andExpect(jsonPath("$.data[0].children[1].photo").value("photo2.png"))
                // 校验第二个根分类节点无子节点且无icon/photo
                .andExpect(jsonPath("$.data[1].id").value(4))
                .andExpect(jsonPath("$.data[1].label").value("根分类2"))
                .andExpect(jsonPath("$.data[1].children").doesNotExist())
                .andExpect(jsonPath("$.data[1].icon").doesNotExist())
                .andExpect(jsonPath("$.data[1].photo").doesNotExist());
    }

    // 测试当没有任何模板分类时返回空列表，期望code=200，data为空列表
    @Test
    void testGetTemplateTreeEmpty() throws Exception {
        when(templateCategoryMapper.selectList(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/templates/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

