package com.example.musicplatform.service.redisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisConnectionChecker {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 检测 Redis 是否可用
     */
    public boolean isRedisConnected() {
        try {
            String result = null;
            if (redisTemplate.getConnectionFactory() != null) {
                result = redisTemplate.getConnectionFactory()
                        .getConnection()
                        .ping();
            }
            return "PONG".equals(result);
        } catch (Exception e) {
            return false;
        }
    }
}