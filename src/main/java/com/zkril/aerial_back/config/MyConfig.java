package com.zkril.aerial_back.config;

import com.zkril.aerial_back.util.JWTInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MyConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 使用 allowedOriginPatterns 替换 allowedOrigins
                .allowedHeaders(CorsConfiguration.ALL)
                .allowedMethods(CorsConfiguration.ALL)
                .allowCredentials(true)
                .maxAge(3600);
    }
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new JWTInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns("/login", "/register","/get_all_products","/send_code",
//                        "/forgot/reset_password","/forgot/send_code"); // 登录、注册等接口排除拦截
//    }
}

