package com.payment.export.platform.application.security.filter;

import com.payment.export.platform.domain.dto.security.JwtToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtUserContextInterceptor implements HandlerInterceptor {

    public static final String JWT_TOKEN_REQUEST_ATTRIBUTE = "auth-token";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtToken jwtToken) {
            request.setAttribute(JWT_TOKEN_REQUEST_ATTRIBUTE, jwtToken);
        }
        return true;
    }
}

