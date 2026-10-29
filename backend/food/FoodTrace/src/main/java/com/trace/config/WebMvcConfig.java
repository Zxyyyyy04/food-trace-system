package com.trace.config;

import com.trace.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/trace/detail/**",
                        "/api/trace/history/**",
                        "/api/statistics/**", // 🌟 新增：放行大屏统计接口
                        "/error",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v2/api-docs",
                        "/swagger-ui.html"
                );
    }
}