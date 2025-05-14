package com.zkril.aerial_back.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Calendar;
import java.util.Map;

public class JWTUtils {

    // 定义秘钥（可以后续改为通过配置文件外部化）
    private static final String SING = "ZKRIL";

    /**
     * 生成 Token，默认有效期为7天（Calendar.DATE, 7）
     */
    public String getToken(Map<String, String> map) {
        Calendar instance = Calendar.getInstance();
        // 设置过期时间为7天后
        instance.add(Calendar.DATE, 7);
        // 创建 jwt builder
        JWTCreator.Builder builder = JWT.create();
        // 添加 payload 中的各个声明
        map.forEach((k, v) -> builder.withClaim(k, v));
        // 设置过期时间并签名
        String token = builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(SING));
        return token;
    }

    /**
     * 验证 token 合法性，若验证不通过会抛出异常
     */
    public static DecodedJWT verify(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SING)).build();
        return verifier.verify(token);
    }
}
