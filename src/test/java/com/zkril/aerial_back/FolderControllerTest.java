package com.zkril.aerial_back;


import com.zkril.aerial_back.mapper.FoldersMapper;
import com.zkril.aerial_back.mapper.ProjectsMapper;
import com.zkril.aerial_back.pojo.Folders;
import com.zkril.aerial_back.pojo.Projects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FolderController测试类。
 * 测试文件夹相关接口，包括创建、删除、重命名、列表查询等功能的成功和失败场景。
 */
@SpringBootTest
@AutoConfigureMockMvc
class FolderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FoldersMapper foldersMapper;
    @MockBean
    private ProjectsMapper projectsMapper;

    // 测试新建文件夹成功，期望返回code=200，data为新文件夹ID，message为“文件夹创建成功”
    @Test
    void testCreateFolderSuccess() throws Exception {
        // 模拟无重名文件夹
        when(foldersMapper.selectOne(any())).thenReturn(null);
        // 模拟insert成功并设置返回的新文件夹ID
        doAnswer(invocation -> {
            Folders folderArg = invocation.getArgument(0);
            folderArg.setId(10); // 模拟数据库生成ID为10
            return 1;
        }).when(foldersMapper).insert(org.mockito.ArgumentMatchers.any(Folders.class));

        mockMvc.perform(post("/folders/create")
                        .param("userId", "1")
                        .param("folderName", "MyFolder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("文件夹创建成功"))
                .andExpect(jsonPath("$.data").value(10));
    }

    // 测试新建文件夹失败（同名文件夹已存在），期望返回code=201，message为“同名文件夹已存在，无法创建”
    @Test
    void testCreateFolderFailDuplicateName() throws Exception {
        // 模拟存在同名文件夹
        Folders existing = new Folders();
        existing.setId(5);
        when(foldersMapper.selectOne(any())).thenReturn(existing);

        mockMvc.perform(post("/folders/create")
                        .param("userId", "1")
                        .param("folderName", "MyFolder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("同名文件夹已存在，无法创建"));

        // 验证未调用insert
        verify(foldersMapper, never()).insert(any());
    }

    // 测试删除文件夹成功，期望返回code=200，data为“空文件夹删除成功”，message为“成功”
    @Test
    void testDeleteFolderSuccess() throws Exception {
        // 模拟文件夹存在且非默认且无项目
        Folders folder = new Folders();
        folder.setId(100);
        folder.setName("普通文件夹");
        when(foldersMapper.selectById(100)).thenReturn(folder);
        when(projectsMapper.selectCount(any())).thenReturn(0L);
        when(foldersMapper.deleteById(100)).thenReturn(1);

        mockMvc.perform(post("/folders/delete")
                        .param("folderId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").value("空文件夹删除成功"));
    }

    // 测试删除文件夹失败（文件夹不存在），期望返回code=201，message为“文件夹不存在”
    @Test
    void testDeleteFolderFailNotFound() throws Exception {
        when(foldersMapper.selectById(101)).thenReturn(null);

        mockMvc.perform(post("/folders/delete")
                        .param("folderId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("文件夹不存在"));
    }

    // 测试删除文件夹失败（默认文件夹不可删除），期望返回code=201，message为“默认文件夹不可删除”
    @Test
    void testDeleteFolderFailDefaultFolder() throws Exception {
        Folders defaultFolder = new Folders();
        defaultFolder.setId(102);
        defaultFolder.setName("默认文件夹");
        when(foldersMapper.selectById(102)).thenReturn(defaultFolder);

        mockMvc.perform(post("/folders/delete")
                        .param("folderId", "102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("默认文件夹不可删除"));
    }

    // 测试删除文件夹失败（文件夹下有项目未删除），期望返回code=201，message为提示先删除项目
    @Test
    void testDeleteFolderFailNotEmpty() throws Exception {
        Folders folder = new Folders();
        folder.setId(103);
        folder.setName("普通文件夹");
        when(foldersMapper.selectById(103)).thenReturn(folder);
        // 模拟文件夹下存在项目
        when(projectsMapper.selectCount(any())).thenReturn(5L);

        mockMvc.perform(post("/folders/delete")
                        .param("folderId", "103"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("请先删除该文件夹下的所有项目后，再删除文件夹"));
    }

    // 测试重命名文件夹成功，期望返回code=200，data为“文件夹重命名成功”，message为“成功”
    @Test
    void testRenameFolderSuccess() throws Exception {
        // 模拟文件夹存在且非默认且无同名冲突
        Folders folder = new Folders();
        folder.setId(200);
        folder.setName("OldName");
        folder.setUserId(1);
        when(foldersMapper.selectById(200)).thenReturn(folder);
        when(foldersMapper.selectOne(any())).thenReturn(null);
        when(foldersMapper.updateById(any())).thenReturn(1);

        mockMvc.perform(post("/folders/rename")
                        .param("folderId", "200")
                        .param("userId", "1")
                        .param("newFolderName", "NewName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data").value("文件夹重命名成功"));
    }

    // 测试重命名文件夹失败（文件夹不存在），期望返回code=201，message为“文件夹不存在”
    @Test
    void testRenameFolderFailNotFound() throws Exception {
        when(foldersMapper.selectById(201)).thenReturn(null);

        mockMvc.perform(post("/folders/rename")
                        .param("folderId", "201")
                        .param("userId", "1")
                        .param("newFolderName", "Name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("文件夹不存在"));
    }

    // 测试重命名文件夹失败（默认文件夹不可改名），期望返回code=201，message为“默认文件夹不可改名”
    @Test
    void testRenameFolderFailDefaultFolder() throws Exception {
        Folders defaultFolder = new Folders();
        defaultFolder.setId(202);
        defaultFolder.setName("默认文件夹");
        when(foldersMapper.selectById(202)).thenReturn(defaultFolder);

        mockMvc.perform(post("/folders/rename")
                        .param("folderId", "202")
                        .param("userId", "1")
                        .param("newFolderName", "Name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("默认文件夹不可改名"));
    }

    // 测试重命名文件夹失败（存在同名文件夹），期望返回code=201，message为“已有同名文件夹，无法重命名”
    @Test
    void testRenameFolderFailDuplicateName() throws Exception {
        // 模拟待重命名文件夹存在且非默认
        Folders folder = new Folders();
        folder.setId(203);
        folder.setName("OldName");
        folder.setUserId(1);
        when(foldersMapper.selectById(203)).thenReturn(folder);
        // 模拟存在同一user下同名文件夹
        Folders conflict = new Folders();
        conflict.setId(204);
        conflict.setName("NewName");
        conflict.setUserId(1);
        when(foldersMapper.selectOne(any())).thenReturn(conflict);

        mockMvc.perform(post("/folders/rename")
                        .param("folderId", "203")
                        .param("userId", "1")
                        .param("newFolderName", "NewName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("失败"))
                .andExpect(jsonPath("$.data").value("已有同名文件夹，无法重命名"));
    }

    // 测试获取文件夹列表成功，期望返回code=200，每个文件夹包含正确的项目数量
    @Test
    void testGetFoldersWithProjectCountSuccess() throws Exception {
        // 准备文件夹列表
        Folders folder1 = new Folders();
        folder1.setId(300);
        folder1.setUserId(1);
        folder1.setName("工作1");
        folder1.setCreateTime(LocalDateTime.now());
        folder1.setUpdateTime(LocalDateTime.now());
        Folders folder2 = new Folders();
        folder2.setId(301);
        folder2.setUserId(1);
        folder2.setName("工作2");
        folder2.setCreateTime(LocalDateTime.now());
        folder2.setUpdateTime(LocalDateTime.now());
        List<Folders> folders = Arrays.asList(folder1, folder2);
        when(foldersMapper.selectList(any())).thenReturn(folders);
        // 准备项目列表（folder1下3个项目，folder2下0个）
        Projects p1 = new Projects(); p1.setFolderId(300);
        Projects p2 = new Projects(); p2.setFolderId(300);
        Projects p3 = new Projects(); p3.setFolderId(300);
        List<Projects> projects = Arrays.asList(p1, p2, p3);
        when(projectsMapper.selectList(any())).thenReturn(projects);

        mockMvc.perform(get("/folders/list")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                // 返回的数据应包含一个根节点"工作文件夹"
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].label").value("工作文件夹"))
                // 根节点children应包含2个文件夹条目
                .andExpect(jsonPath("$.data[0].children.length()").value(2))
                // 校验第一个文件夹信息及项目数量
                .andExpect(jsonPath("$.data[0].children[0].id").value(300))
                .andExpect(jsonPath("$.data[0].children[0].label").value("工作1"))
                .andExpect(jsonPath("$.data[0].children[0].number").value(3))
                // 校验第二个文件夹信息及项目数量为0
                .andExpect(jsonPath("$.data[0].children[1].id").value(301))
                .andExpect(jsonPath("$.data[0].children[1].label").value("工作2"))
                .andExpect(jsonPath("$.data[0].children[1].number").value(0));
    }

    // 测试当用户没有任何文件夹时返回的结构，期望code=200，data包含“工作文件夹”节点但其children为空
    @Test
    void testGetFoldersWithProjectCountEmpty() throws Exception {
        when(foldersMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(projectsMapper.selectList(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/folders/list")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].label").value("工作文件夹"))
                .andExpect(jsonPath("$.data[0].children.length()").value(0));
    }

    // 测试缺少参数时的行为，期望返回HTTP 400错误
    @Test
    void testCreateFolderFailMissingParam() throws Exception {
        // 未提供folderName参数
        mockMvc.perform(post("/folders/create")
                        .param("userId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("")));
    }
}

