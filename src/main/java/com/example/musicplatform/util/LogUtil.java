package com.example.musicplatform.util;

import com.example.musicplatform.service.impl.PostCommentServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    public static void redisFailLog(){
        logger.info("链接不到redis,降级到数据库");
    }
}
