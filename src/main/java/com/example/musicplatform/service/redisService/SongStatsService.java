package com.example.musicplatform.service.redisService;


import com.example.musicplatform.dto.RedisStats.SongStats;
import com.example.musicplatform.entity.Song;
import com.example.musicplatform.repository.SongRepository;
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
        double hotScore=calculateHotScore(fc,cc,pc,song.getCreatedAt());
        return new SongStats(songId,pc,fc,cc,hotScore);
    }
    private double calculateHotScore(Long fc, Long cc, Long pc, LocalDateTime createTime){
        double playScore = Math.log1p(pc);//平滑播放量
        double favRate = pc>0?(double)fc/pc:0;
        double commentRate = pc>0?(double) cc/pc:0;

        double baseScore = playScore+favRate*100*5.0+commentRate*100*3.0;
        long hoursOld = ChronoUnit.HOURS.between(createTime,LocalDateTime.now());
        double timeDecay;
        if(hoursOld<=72){
            timeDecay = 1.0-(hoursOld/72.0)*0.1;
        }else {
            timeDecay = 0.9*Math.pow(0.98,hoursOld-72);
        }
        return baseScore*timeDecay;
    }
}
