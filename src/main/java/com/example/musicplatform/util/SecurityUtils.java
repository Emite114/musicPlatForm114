package com.example.musicplatform.util;

import com.example.musicplatform.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具类：获取当前登录用户信息
 */
@Component
public class SecurityUtils {
    /**
     * 获取当前登录用户的ID
     * @return 用户ID（未登录抛异常）
     */
    public static Long getCurrentUserId() {
        // 1. 获取SecurityContext中的Authentication对象
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("用户未登录");
        }
        // 2. 提取用户ID（注意：principal的类型要和过滤器中封装的一致）
        CustomUserDetails customUserDetails= (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUserId();
    }

    /**
     * 安全获取用户ID（未登录返回null，不抛异常）
     */
    public static Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        try {
            CustomUserDetails customUserDetails= (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getUserId();
        } catch (Exception e) {
            return null;
        }
    }
}
