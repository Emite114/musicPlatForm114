package com.example.musicplatform.task;

import com.example.musicplatform.dto.RedisStats.PostStats;
import com.example.musicplatform.dto.RedisStats.SongStats;
import com.example.musicplatform.entity.Post;
import com.example.musicplatform.entity.Song;
import com.example.musicplatform.repository.PostRepository;
import com.example.musicplatform.repository.SongRepository;
import com.example.musicplatform.service.redisService.PostStatsService;
import com.example.musicplatform.service.redisService.RedisConnectionChecker;
import com.example.musicplatform.service.redisService.SongStatsService;


import com.example.musicplatform.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Component
public class StatsSyncTask {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private PostStatsService postStatsService;
    @Autowired
    private SongStatsService songStatsService;
    @Autowired
    private RedisConnectionChecker redisConnectionChecker;


    @Transactional
    @Scheduled(fixedRate = 1000)
    public void syncPostStatsToDatabase() {
        if (!redisConnectionChecker.isRedisConnected()) {
            LogUtil.redisFailLog();
            return;
        }
        Set<String> keys = stringRedisTemplate.keys("post:*");
        if(keys == null || keys.isEmpty()){
            return;
        }
        for (String key : keys) {
            String[] parts = key.split(":");
            Long postId = Long.valueOf(parts[2]);
            PostStats postStats = postStatsService.getNewPostStats(postId);
            Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("post not found"));
            post.setViewCount(postStats.getNewViewCount());
            post.setCommentCount(postStats.getNewCommentCount());
            post.setLikeCount(postStats.getNewLikeCount());
            post.setHotScore(postStats.getNewHotScore());
            postRepository.save(post);
        }
    }
    @Transactional
    @Scheduled(fixedRate = 1000)
    public void syncSongStatsToDatabase() {
        if(!redisConnectionChecker.isRedisConnected()) {
            return;
        }
        Set<String> keys = stringRedisTemplate.keys("song:*");
        if(keys == null || keys.isEmpty()){
            return;
        }
        for (String key : keys) {
            String[] parts = key.split(":");
            Long songId = Long.valueOf(parts[2]);
            Song song = songRepository.findById(songId).orElseThrow(() -> new RuntimeException("song not found"));
            SongStats songStats = songStatsService.getNewSongStats(songId);
            song.setPlayCount(songStats.getNewPlayCount());
            song.setCommentCount(songStats.getNewCommentCount());
            song.setFavouriteCount(songStats.getNewFavouriteCount());
            song.setHotScore(songStats.getNewHotScore());
            songRepository.save(song);
        }
    }

}
