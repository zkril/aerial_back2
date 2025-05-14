package com.zkril.aerial_back.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.zkril.aerial_back.util.JWTUtils;
import com.zkril.aerial_back.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    /**
     * 测试接口，用于检验 token 是否正确
     * 请求头中需包含 token (key: "token")
     */
    @GetMapping("/testAuth")
    public Result testAuth(@RequestHeader("token") String token) {

        DecodedJWT decodedJWT = JWTUtils.verify(token);
        if (decodedJWT == null) {
            return Result.fail();
        }
        String userId = decodedJWT.getClaim("userId").asString();
        String userName = decodedJWT.getClaim("userName").asString();
        return Result.ok("Token valid. UserId: " + userId + ", UserName: " + userName);
    }
}

