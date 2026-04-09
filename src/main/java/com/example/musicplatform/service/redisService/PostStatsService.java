package com.example.musicplatform.service.redisService;

import com.example.musicplatform.dto.RedisStats.PostStats;
import com.example.musicplatform.entity.Post;
import com.example.musicplatform.repository.PostRepository;
import com.example.musicplatform.util.CalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class PostStatsService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PostRepository  postRepository;

    public void increasePostViewCount(Long postId) {
        redisTemplate.opsForValue().increment("post:viewCount"+postId,1L);
    }

    public void increasePostLikeCount(Long postId) {
        redisTemplate.opsForValue().increment("post:likeCount"+postId,1L);
    }
    public void decreasePostLikeCount(Long postId) {
        redisTemplate.opsForValue().increment("post:likeCount"+postId,-1L);
    }

    public void increasePostCommentCount(Long postId) {
        redisTemplate.opsForValue().increment("post:commentCount"+postId,1L);
    }
    public void decreasePostCommentCount(Long postId) {
        redisTemplate.opsForValue().increment("post:commentCount"+postId,-1L);
    }

    public void increasePostFavouriteCount(Long postId) {
        redisTemplate.opsForValue().increment("post:favouriteCount"+postId,1L);
    }
    public void decreasePostFavouriteCount(Long postId) {
        redisTemplate.opsForValue().increment("post:favouriteCount"+postId,-1L);
    }


    public Long getIncrement(String key){
        String value = redisTemplate.opsForValue().get(key);
        return value==null?0L:Long.parseLong(value);
    }

    public PostStats getNewPostStats(Long postId){
    Optional<Post> existing = postRepository.findById(postId);
    Post post = existing.orElseThrow(()->new RuntimeException(" 意外的错误,找不到帖子"));
    Long newViewCount =post.getViewCount()+getIncrement("post:viewCount"+postId);
    Long newLikeCount = post.getLikeCount()+getIncrement("post:likeCount"+postId);
    Long newCommentCount = post.getCommentCount()+getIncrement("post:commentCount"+postId);
    Long newFavouriteCount = post.getFavouriteCount()+getIncrement("post:favouriteCount"+postId);
    double hotScore = CalculateUtil.calculatePostHotScore(newViewCount,newLikeCount,newCommentCount,newFavouriteCount,post.getCreateTime());

    return new PostStats(postId,newViewCount,newLikeCount,newFavouriteCount,newCommentCount,hotScore);
    }

}
