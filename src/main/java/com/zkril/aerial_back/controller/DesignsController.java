package com.zkril.aerial_back.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zkril.aerial_back.mapper.DesignsMapper;
import com.zkril.aerial_back.mapper.ProductsMapper;
import com.zkril.aerial_back.mapper.UserFavoriteMapper;
import com.zkril.aerial_back.pojo.Designs;
import com.zkril.aerial_back.pojo.Products;
import com.zkril.aerial_back.pojo.UserFavorite;
import com.zkril.aerial_back.util.JWTUtils;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.ls.LSInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class DesignsController {

    @Autowired
    private DesignsMapper designsMapper;
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    @Value("${app.image-base-url}")
    private String imageBaseUrl;
    @GetMapping("/get_all_designs")
    public Result getAllDesigns() {
        List<Designs> designsList =designsMapper.selectList(null);
        List<Map<String, Object>> data = new ArrayList<>();

        // 遍历每个产品，组装符合要求的返回数据
        for (Designs design : designsList) {
            Map<String, Object> designMap = new HashMap<>();
            designMap.put("id", design.getDesignId());
            designMap.put("type","design");
            designMap.put("name", design.getName());
            designMap.put("shape",design.getShape());
            // 将 photo 字符串（以逗号分隔）转换为 List<String>
            designMap.put("photo", splitToListI(design.getPhoto()));
            designMap.put("intro", design.getIntro());
            designMap.put("desc", design.getDescp());
            designMap.put("homePhoto", imageBaseUrl +design.getHomePhoto());
            // 处理 data1 部分字段
            Map<String, Object> data1 = new HashMap<>();
            data1.put("name", splitToList(design.getData1Name()));
            data1.put("min", splitToList(design.getData1Min()));
            data1.put("normal", splitToList(design.getData1Normal()));
            data1.put("max", splitToList(design.getData1Max()));
            designMap.put("data1", data1);

            // 处理 data2 部分字段
            Map<String, Object> data2 = new HashMap<>();
            data2.put("name", splitToList(design.getData2Name()));
            data2.put("text", splitToList(design.getData2Text()));
            designMap.put("data2", data2);

            designMap.put("link", design.getLink());
            designMap.put("3DM",imageBaseUrl +design.getModel3d());
            designMap.put("3DE",imageBaseUrl +design.getEnhance3d());
            data.add(designMap);
        }
        return Result.ok(data);
    }

    @GetMapping("/get_designs")
    public Result getProductById(@RequestParam Integer id, @RequestHeader("token") String token) {
        DecodedJWT decodedJWT = JWTUtils.verify(token);
        if (decodedJWT == null) {
            return Result.fail();
        }
        String userId = decodedJWT.getClaim("userId").asString();
        // 根据 id 查询单个产品记录
        Designs design = designsMapper.selectById(id);
        if (design == null) {
            return Result.fail("未找到设计");
        }

        Map<String, Object> designMap = new HashMap<>();
        designMap.put("id", design.getDesignId());
        designMap.put("name", design.getName());
        designMap.put("type","design");
        designMap.put("shape",design.getShape());
        // 将 photo 字符串（以逗号分隔）转换为 List<String>
        designMap.put("photo", splitToListI(design.getPhoto()));
        designMap.put("intro", design.getIntro());
        designMap.put("desc", design.getDescp());
        designMap.put("homePhoto", imageBaseUrl +design.getHomePhoto());
        // 处理 data1 部分字段
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", splitToList(design.getData1Name()));
        data1.put("min", splitToList(design.getData1Min()));
        data1.put("normal", splitToList(design.getData1Normal()));
        data1.put("max", splitToList(design.getData1Max()));
        data1.put("unit", splitToList(design.getData1Unit()));
        designMap.put("data1", data1);

        // 处理 data2 部分字段
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", splitToList(design.getData2Name()));
        data2.put("text", splitToList(design.getData2Text()));
        designMap.put("data2", data2);
        boolean star=false;
        LambdaQueryWrapper<UserFavorite> userFavoriteLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFavoriteLambdaQueryWrapper.eq(UserFavorite::getUserId, userId);
        userFavoriteLambdaQueryWrapper.eq(UserFavorite::getType,"design");
        userFavoriteLambdaQueryWrapper.eq(UserFavorite::getTargetId,id);
        UserFavorite userFavorite=userFavoriteMapper.selectOne(userFavoriteLambdaQueryWrapper);
        if (userFavorite != null) {
            star=true;
        }
        designMap.put("isStar", star);
        designMap.put("link", design.getLink());
        designMap.put("3DM",imageBaseUrl +design.getModel3d());
        designMap.put("3DE",imageBaseUrl +design.getEnhance3d());
        return Result.ok(designMap);
    }


    /**
     * 辅助函数，将以逗号分隔的字符串拆分成 List<String>
     */
    private List<String> splitToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 如果数据中有多余的空格，可以通过 trim() 去除
        String[] arr = str.split(",");
        List<String> list = new ArrayList<>();
        for (String s : arr) {
            list.add(s.trim());
        }
        return list;
    }
    /**
     * 辅助函数，将以逗号分隔的字符串拆分成 List<String>
     */
    private List<String> splitToListI(String str) {
        if (str == null || str.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 如果数据中有多余的空格，可以通过 trim() 去除
        String[] arr = str.split(",");
        List<String> list = new ArrayList<>();
        for (String s : arr) {
            list.add(imageBaseUrl +s.trim());
        }
        return list;
    }
}