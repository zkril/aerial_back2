package com.zkril.aerial_back.config;

import com.zkril.aerial_back.util.JWTInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pro-image/**")
                .addResourceLocations("file:C:/aerialdoc/pro-image/");
        registry.addResourceHandler("/des-image/**")
                .addResourceLocations("file:C:/aerialdoc/des-image/");
        registry.addResourceHandler("/comment-image/**")
                .addResourceLocations("file:C:/aerialdoc/comment-image/");
        registry.addResourceHandler("/user-image/**")
                .addResourceLocations("file:C:/aerialdoc/user-image/");
    }
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new JWTInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns("/login", "/register","/get_all_products","/send_code",
//                        "/forgot/reset_password","/forgot/send_code","/get_all_designs"
//                        ,"/pro-image/**","/des-image/**","/comment-image/**","/user-image/**"); // 登录、注册等接口排除拦截
//    }
}

