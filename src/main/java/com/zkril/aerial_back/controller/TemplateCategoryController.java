package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.TemplateCategoryMapper;
import com.zkril.aerial_back.pojo.TemplateCategory;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/templates")
public class TemplateCategoryController {

    @Autowired
    private TemplateCategoryMapper templateCategoryMapper;

    @GetMapping("/tree")
    public Result getTemplateTree() {
        List<TemplateCategory> categories = templateCategoryMapper.selectList(null);

        // 先按 parent_id 组织成 Map
        Map<Integer, List<TemplateCategory>> parentChildMap = categories.stream()
                .collect(Collectors.groupingBy(c -> Optional.ofNullable(c.getParentId()).orElse(0)));

        // 递归构建树
        List<Map<String, Object>> tree = buildTree(parentChildMap, 0);

        return Result.ok(tree);
    }

    private List<Map<String, Object>> buildTree(Map<Integer, List<TemplateCategory>> parentChildMap, Integer parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<TemplateCategory> children = parentChildMap.get(parentId);
        if (children != null) {
            for (TemplateCategory child : children) {
                Map<String, Object> node = new HashMap<>();
                node.put("label", child.getName());
                node.put("name", child.getTemplateType());
                node.put("id", child.getId());
                if (child.getIcon() != null){
                    node.put("icon", child.getIcon());
                }
                if (child.getPhoto() != null) {
                    node.put("photo", child.getPhoto());
                }
                // 递归
                List<Map<String, Object>> subChildren = buildTree(parentChildMap, child.getId());
                if (!subChildren.isEmpty()) {
                    node.put("children", subChildren);
                }
                result.add(node);
            }
        }
        return result;
    }
}
