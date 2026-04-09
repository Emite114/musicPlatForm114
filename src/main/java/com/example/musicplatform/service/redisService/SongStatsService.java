package com.example.musicplatform.service.redisService;


import com.example.musicplatform.dto.RedisStats.SongStats;
import com.example.musicplatform.entity.Song;
import com.example.musicplatform.repository.SongRepository;
import com.example.musicplatform.util.CalculateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class SongStatsService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SongRepository songRepository;

    public void increaseSongFavouriteCount(Long songId) {
        redisTemplate.opsForValue().increment("song:favouriteCount"+songId,1L);
    }
    public void decreaseSongFavouriteCount(Long songId) {
        redisTemplate.opsForValue().increment("song:favouriteCount"+songId,-1L);
    }
    public void increaseSongCommentCount(Long songId) {
        redisTemplate.opsForValue().increment("song:commentCount"+songId,1L);
    }
    public void decreaseSongCommentCount(Long songId) {
        redisTemplate.opsForValue().increment("song:commentCount"+songId,-1L);
    }
    public void increaseSongPlayCount(Long songId) {
        redisTemplate.opsForValue().increment("song:playCount"+songId,1L);
    }
    public Long getIncrement(String key){
        String value = redisTemplate.opsForValue().get(key);
        return value==null?0L:Long.parseLong(value);
    }

    public SongStats getNewSongStats(Long songId){
        Optional<Song> existing = songRepository.findById(songId);
        if(existing.isEmpty()){
            throw new RuntimeException("Song not found");
        }
        Song song = existing.orElseThrow(()->new RuntimeException("意外的错误"));
       //收藏量
        Long fc = song.getFavouriteCount()+getIncrement("song:favouriteCount"+songId);
       //评论量
        Long cc = song.getCommentCount()+getIncrement("song:commentCount"+songId);
       //播放量
        Long pc = song.getPlayCount()+getIncrement("song:playCount"+songId);
        double hotScore= CalculateUtil.calculateSongHotScore(fc,cc,pc,song.getCreateTime());
        return new SongStats(songId,pc,fc,cc,hotScore);
    }

}
