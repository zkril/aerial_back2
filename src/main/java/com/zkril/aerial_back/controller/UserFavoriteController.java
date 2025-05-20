package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkril.aerial_back.mapper.DesignsMapper;
import com.zkril.aerial_back.mapper.ProductsMapper;
import com.zkril.aerial_back.mapper.UserFavoriteMapper;
import com.zkril.aerial_back.pojo.Designs;
import com.zkril.aerial_back.pojo.Products;
import com.zkril.aerial_back.pojo.UserFavorite;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    @Autowired
    private DesignsMapper designsMapper;
    @Autowired
    private ProductsMapper productsMapper;;
    @Value("${app.image-base-url}")
    private String imageBaseUrl;
    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result addFavorite(@RequestParam Long userId,
                              @RequestParam Long targetId,
                              @RequestParam String type) {
        // 检查是否已收藏
        QueryWrapper<UserFavorite> query = new QueryWrapper<UserFavorite>()
                .eq("user_id", userId)
                .eq("target_id", targetId)
                .eq("type", type);

        if (userFavoriteMapper.selectCount(query) > 0) {
            return Result.fail("已收藏");
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetId(targetId);
        favorite.setType(type);
        favorite.setCreatedTime(LocalDateTime.now());
        userFavoriteMapper.insert(favorite);
        return Result.ok();
    }

    /**
     * 删除收藏
     */
    @PostMapping("/delete")
    public Result deleteFavorite(@RequestParam Long userId,
                                 @RequestParam Long targetId,
                                 @RequestParam String type) {
        int rows = userFavoriteMapper.delete(new QueryWrapper<UserFavorite>()
                .eq("user_id", userId)
                .eq("target_id", targetId)
                .eq("type", type));
        if (rows > 0) {
            return Result.ok();
        } else {
            return Result.fail("未找到该收藏");
        }
    }

    /**
     * 查询我的所有收藏（可分类）
     */
    @GetMapping("/list")
    public Result listFavorites(@RequestParam Long userId,
                                @RequestParam(required = false) String type) {

        QueryWrapper<UserFavorite> query = new QueryWrapper<UserFavorite>()
                .eq("user_id", userId);
        if (type != null && !type.isEmpty()) {
            query.eq("type", type);
        }
        List<UserFavorite> favorites = userFavoriteMapper.selectList(query.orderByDesc("created_time"));

        List<Map<String, Object>> resultList = new ArrayList<>();

        for (UserFavorite fav : favorites) {
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", fav.getId());
            item.put("userId", fav.getUserId());
            item.put("type", fav.getType());
            item.put("targetId", fav.getTargetId());
            item.put("createdTime", fav.getCreatedTime());

            // 根据type查询详细信息
            if ("product".equalsIgnoreCase(fav.getType())) {
                Products physical = productsMapper.selectById(fav.getTargetId());
                item.put("detailName", physical.getName());
                item.put("detailId",physical.getProductId());
                item.put("detailHomePhoto",imageBaseUrl+physical.getHomePhoto());
            } else if ("design".equalsIgnoreCase(fav.getType())) {
                Designs design = designsMapper.selectById(fav.getTargetId());
                item.put("detailName", design.getName());
                item.put("detailId",design.getDesignId());
                item.put("detailHomePhoto",imageBaseUrl+design.getHomePhoto());
            } else {
                item.put("detail", null); // 其他类型备用
            }
            resultList.add(item);
        }

        return Result.ok(resultList);
    }

}

