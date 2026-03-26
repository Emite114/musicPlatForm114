package com.example.musicplatform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String PREFIX = "logout:token:";

    // 加入黑名单
    public void addToBlacklist(String token, long expireTime) {
        redisTemplate.opsForValue().set(
                PREFIX + token,
                "1",
                expireTime,
                TimeUnit.MILLISECONDS
        );
    }

    // 判断是否在黑名单
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        } catch (Exception e) {

            return false;
        }
    }
}
