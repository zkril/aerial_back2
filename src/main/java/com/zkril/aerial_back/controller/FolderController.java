package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.FoldersMapper;
import com.zkril.aerial_back.mapper.ProjectsMapper;
import com.zkril.aerial_back.pojo.Folders;
import com.zkril.aerial_back.pojo.Projects;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/folders")
public class FolderController {

    @Autowired
    private FoldersMapper foldersMapper;
    @Autowired
    private ProjectsMapper projectsMapper;

    // 新建文件夹
    @PostMapping("/create")
    public Result createFolder(@RequestParam Integer userId, @RequestParam String folderName) {
        // 1. 先检查是否已经存在同名文件夹
        QueryWrapper<Folders> query = new QueryWrapper<>();
        query.eq("user_id", userId)
                .eq("name", folderName);

        Folders existingFolder = foldersMapper.selectOne(query);

        if (existingFolder != null) {
            return Result.fail("同名文件夹已存在，无法创建");
        }

        // 2. 不存在才创建新的文件夹
        Folders folder = new Folders();
        folder.setUserId(userId);
        folder.setName(folderName);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());

        foldersMapper.insert(folder);

        return Result.ok(folder.getId()).message("文件夹创建成功");
    }
    @PostMapping("/delete")
    public Result deleteFolder(@RequestParam Integer folderId) {
        // 1. 检查文件夹是否存在
        Folders folder = foldersMapper.selectById(folderId);
        if (folder == null) {
            return Result.fail("文件夹不存在");
        }
        if (folder.getName().equals("默认文件夹")){
            return Result.fail("默认文件夹不可删除");
        }
        // 2. 检查文件夹下是否有项目
        Long projectCount = Long.valueOf(projectsMapper.selectCount(new QueryWrapper<Projects>().eq("folder_id", folderId)));
        if (projectCount > 0) {
            return Result.fail("请先删除该文件夹下的所有项目后，再删除文件夹");
        }

        // 3. 删除空文件夹
        foldersMapper.deleteById(folderId);

        return Result.ok("空文件夹删除成功");
    }
    @PostMapping("/rename")
    public Result renameFolder(
            @RequestParam Integer folderId,
            @RequestParam Integer userId,
            @RequestParam String newFolderName) {

        // 1. 先检查文件夹是否存在
        Folders folder = foldersMapper.selectById(folderId);
        if (folder == null) {
            return Result.fail("文件夹不存在");
        }
        if (folder.getName().equals("默认文件夹")){
            return Result.fail("默认文件夹不可改名");
        }
        // 2. 检查新名字是否冲突（同一个userId下）
        QueryWrapper<Folders> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("name", newFolderName);

        Folders existingFolder = foldersMapper.selectOne(query);
        if (existingFolder != null) {
            return Result.fail("已有同名文件夹，无法重命名");
        }

        // 3. 修改名字和更新时间
        folder.setName(newFolderName);
        folder.setUpdateTime(LocalDateTime.now());
        foldersMapper.updateById(folder);

        return Result.ok("文件夹重命名成功");
    }
    // 获取当前用户的文件夹列表，并统计每个文件夹下的项目数量
    @GetMapping("/list")
    public Result getFoldersWithProjectCount(@RequestParam Integer userId) {
        // 1. 查询该用户所有文件夹
        List<Folders> folders = foldersMapper.selectList(
                new QueryWrapper<Folders>().eq("user_id", userId)
        );

        // 2. 查询该用户所有项目
        List<Projects> projects = projectsMapper.selectList(
                new QueryWrapper<Projects>().eq("user_id", userId)
        );

        // 3. 遍历组装结果
        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Folders folder : folders) {
            Map<String, Object> folderInfo = new HashMap<>();
            folderInfo.put("id", folder.getId());
            folderInfo.put("label", folder.getName());

            // 计算该文件夹下的项目数量
            int count = 0;
            for (Projects project : projects) {
                if (project.getFolderId().equals(folder.getId())) {
                    count++;
                }
            }
            folderInfo.put("number", count);

            result.add(folderInfo);
        }
        map.put("id",1);
        map.put("label","工作文件夹");
        map.put("children", result);
        maps.add(map);
        return Result.ok(maps);
    }
    @GetMapping("/all")
    public Result getAllFolders(@RequestParam Integer userId) {
        List<Folders> folders = foldersMapper.selectList(
                new QueryWrapper<Folders>().eq("user_id", userId)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Folders folder : folders) {
            Map<String, Object> folderInfo = new HashMap<>();
            folderInfo.put("folderId", folder.getId());
            folderInfo.put("folderName", folder.getName());
            folderInfo.put("createTime", folder.getCreateTime());
            folderInfo.put("lastEditTime", folder.getUpdateTime());
            result.add(folderInfo);
        }

        return Result.ok(result);
    }
}
