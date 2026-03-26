package com.example.musicplatform.config;

import com.example.musicplatform.service.impl.TokenBlacklistService;
import com.example.musicplatform.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklistService tokenBlacklistService;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;


        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            // 移除 "Bearer " 前缀
            if (tokenBlacklistService.isBlacklisted(jwtToken)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("jwtToken 已被加入黑名单");
                return;
            }
            try {
                // 2. 从 Token 中解析出用户名
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.err.println("无法获取jwtToken");
            } catch (ExpiredJwtException e) {
                System.err.println("JWT Token has expired");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired");
                return;
            }
        }

        // 3. 如果成功解析出用户名，并且 SecurityContext 中还没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. 从数据库/缓存中加载用户详细信息
            CustomUserDetails customUserDetails = this.customUserDetailsService.loadUserByUsername(username);

            // 5. 验证 JWT Token 是否有效
            if (jwtUtil.validateToken(jwtToken)) {

                // 6. 创建认证令牌并设置到 SecurityContext 中
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        customUserDetails, null, customUserDetails.getAuthorities());

                // 设置请求详情（如 IP 地址、sessionId 等）
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息设置到 SecurityContext 中，表示当前用户已认证
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 7. 继续执行过滤链中的下一个过滤器
        filterChain.doFilter(request, response);
    }


}
