package com.example.musicplatform.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CalculateUtil {
    public static double calculateSongHotScore(Long fc, Long cc, Long pc, LocalDateTime createTime){
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
    public static double calculatePostHotScore(Long vc, Long lc, Long cc, Long fc, LocalDateTime createTime){
        double viewScore = Math.log1p(vc);
        double likeRate = vc>0?(double) lc/vc:0;
        double commentRate = vc>0?(double) cc/vc:0;
        double favouriteRate = vc>0?(double) fc/vc:0;

        double baseScore = viewScore+likeRate*100*3+commentRate*100*4+favouriteRate*100*5;

        long hoursOld = ChronoUnit.HOURS.between(createTime, Instant.now());
        double timeDecay;

        if (hoursOld <= 72) {
            // 72小时内：0.9 ~ 1.0
            timeDecay = 1.0 - (hoursOld / 72.0) * 0.1;
        } else {
            // 72小时后：每小时衰减 2%
            timeDecay = 0.9 * Math.pow(0.98, hoursOld - 72);
        }

        return baseScore * timeDecay;
    }
}
