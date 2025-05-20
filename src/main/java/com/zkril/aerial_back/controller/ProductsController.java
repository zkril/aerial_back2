package com.zkril.aerial_back.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zkril.aerial_back.mapper.ProductsMapper;
import com.zkril.aerial_back.mapper.UserFavoriteMapper;
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

import java.util.*;  // 包含 List、Arrays、ArrayList、HashMap

@RestController
public class ProductsController {

    @Autowired
    private ProductsMapper productsMapper;
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    @Value("${app.image-base-url}")
    private String imageBaseUrl;
    /**
     * 获取所有产品信息，并返回格式：
     * {
     *   "success": true,
     *   "data": [
     *       {
     *         "id": 1,
     *         "name": "喇叭天线",
     *         "photo": ["http://xxxxx.jpg"],
     *         "intro": "...",
     *         "data1": {
     *             "name": ["工作频率", "增益", "驻波比"],
     *             "min": ["240", "null", "24"],
     *             "normal": ["~", "10", "2.0"],
     *             "max": ["6000", "null", "null"],
     *             "unit": ["MHz", "dBi", "null"]
     *         },
     *         "data2": {
     *             "name": ["极化方式", "接口形式", "接口材质"],
     *             "text": ["线极化", "N-K", "外壳304不锈钢"]
     *         },
     *         "buylink": ""
     *       },
     *       ...
     *    ],
     *    "msg": null
     * }
     */
    @GetMapping("/get_all_products")
    public Result getAllProducts() {

        // 从数据库查询所有产品记录
        List<Products> productList = productsMapper.selectList(null);
        List<Map<String, Object>> data = new ArrayList<>();

        // 遍历每个产品，组装符合要求的返回数据
        for (Products product : productList) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getProductId());
            productMap.put("type","product");
            productMap.put("name", product.getName());
            // 将 photo 字符串（以逗号分隔）转换为 List<String>
            productMap.put("photo", splitToListI(product.getPhoto()));
            productMap.put("intro", product.getIntro());
            productMap.put("desc", product.getDescp());
            productMap.put("homePhoto", imageBaseUrl +product.getHomePhoto());
            // 处理 data1 部分字段
            Map<String, Object> data1 = new HashMap<>();
            data1.put("name", splitToList(product.getData1Name()));
            data1.put("min", splitToList(product.getData1Min()));
            data1.put("normal", splitToList(product.getData1Normal()));
            data1.put("max", splitToList(product.getData1Max()));
            data1.put("unit", splitToList(product.getData1Unit()));
            productMap.put("data1", data1);

            // 处理 data2 部分字段
            Map<String, Object> data2 = new HashMap<>();
            data2.put("name", splitToList(product.getData2Name()));
            data2.put("text", splitToList(product.getData2Text()));
            productMap.put("data2", data2);

            productMap.put("buylink", product.getBuylink());
            data.add(productMap);
        }
        return Result.ok(data);
    }

    @GetMapping("/get_product")
    public Result getProductById(@RequestParam Integer id, @RequestHeader("token") String token) {
        DecodedJWT decodedJWT = JWTUtils.verify(token);
        if (decodedJWT == null) {
            return Result.fail();
        }
        String userId = decodedJWT.getClaim("userId").asString();
        // 根据 id 查询单个产品记录
        Products product = productsMapper.selectById(id);
        if (product == null) {
            return Result.fail("未找到产品");
        }

        // 构建返回数据格式
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getProductId());
        productMap.put("name", product.getName());
        productMap.put("type","product");
        // 将 photo 字符串（以逗号分隔）转换为 List<String>
        productMap.put("photo", splitToListI(product.getPhoto()));
        productMap.put("intro", product.getIntro());
        productMap.put("desc", product.getDescp());
        productMap.put("homePhoto", imageBaseUrl+product.getHomePhoto());

        // 处理 data1 部分字段
        Map<String, Object> data1 = new HashMap<>();
        data1.put("name", splitToList(product.getData1Name()));
        data1.put("min", splitToList(product.getData1Min()));
        data1.put("normal", splitToList(product.getData1Normal()));
        data1.put("max", splitToList(product.getData1Max()));
        data1.put("unit", splitToList(product.getData1Unit()));
        productMap.put("data1", data1);

        // 处理 data2 部分字段
        Map<String, Object> data2 = new HashMap<>();
        data2.put("name", splitToList(product.getData2Name()));
        data2.put("text", splitToList(product.getData2Text()));
        productMap.put("data2", data2);

        boolean star=false;
        LambdaQueryWrapper<UserFavorite> userFavoriteLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFavoriteLambdaQueryWrapper.eq(UserFavorite::getUserId, userId);
        userFavoriteLambdaQueryWrapper.eq(UserFavorite::getType,"product");
        userFavoriteLambdaQueryWrapper.eq(UserFavorite::getTargetId,id);
        UserFavorite userFavorite=userFavoriteMapper.selectOne(userFavoriteLambdaQueryWrapper);
        if (userFavorite != null) {
            star=true;
        }
        productMap.put("isStar", star);
        productMap.put("buylink", product.getBuylink());
        return Result.ok(productMap);
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
