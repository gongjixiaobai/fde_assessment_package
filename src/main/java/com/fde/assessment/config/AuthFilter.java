package com.fde.assessment.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 简单认证过滤器 — 加分项 5
 * 通过 X-Auth-Token 头部传递认证令牌，存入请求属性供后续使用
 * 不阻断请求，仅记录用户上下文
 */
@Component
@Order(1)
public class AuthFilter implements Filter {

    private static final String AUTH_HEADER = "X-Auth-Token";
    private static final String USER_ATTR = "currentUser";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String token = httpRequest.getHeader(AUTH_HEADER);

        if (token != null && !token.isEmpty()) {
            // 简化的令牌校验：token = "user:{username}"
            if (token.startsWith("user:")) {
                String username = token.substring(5);
                httpRequest.setAttribute(USER_ATTR, username);
            } else {
                // 令牌格式不正确，但仍放行（演示环境）
                httpRequest.setAttribute(USER_ATTR, "anonymous");
            }
        } else {
            // 无认证令牌，视为匿名用户
            httpRequest.setAttribute(USER_ATTR, "anonymous");
        }

        chain.doFilter(request, response);
    }

    /**
     * 从请求中提取当前用户名
     */
    public static String getCurrentUser(HttpServletRequest request) {
        Object user = request.getAttribute(USER_ATTR);
        return user != null ? user.toString() : "anonymous";
    }
}
