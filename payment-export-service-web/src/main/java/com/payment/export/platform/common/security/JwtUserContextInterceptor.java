package com.payment.export.platform.common.security;

import com.payment.export.platform.common.dto.JwtToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtUserContextInterceptor implements HandlerInterceptor {

    public static final String JWT_TOKEN_REQUEST_ATTRIBUTE = "jwtToken";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtToken jwtToken) {
            request.setAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE, jwtToken);
        }
        return true;
    }
}

