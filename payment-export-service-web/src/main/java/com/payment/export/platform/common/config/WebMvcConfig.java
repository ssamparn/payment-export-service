package com.payment.export.platform.common.config;

import com.payment.export.platform.common.security.JwtUserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUserContextInterceptor jwtUserContextInterceptor;

    public WebMvcConfig(JwtUserContextInterceptor jwtUserContextInterceptor) {
        this.jwtUserContextInterceptor = jwtUserContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtUserContextInterceptor);
    }
}

