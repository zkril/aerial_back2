package com.zkril.aerial_back.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import com.auth0.jwt.exceptions.SignatureVerificationException;

public class JWTInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是预检请求，直接放行并返回 200 OK 状态码
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 获取 token
        String token = request.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"code\": 401, \"message\": \"缺少Token\"}");
            return false;
        }

        try {
            JWTUtils.verify(token);
            return true; // 验证成功，放行请求
        } catch (SignatureVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"code\": 401, \"message\": \"无效签名\"}");
            return false;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"code\": 401, \"message\": \"Token验证失败\"}");
            return false;
        }
    }
}
