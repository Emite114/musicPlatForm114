package com.example.musicplatform.dto.RedisStats;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class PostStats {
    private Long Id;
    private Long newViewCount;
    private Long newLikeCount;
    private Long newFavoriteCount;
    private Long newCommentCount;
    private double newHotScore;
}
