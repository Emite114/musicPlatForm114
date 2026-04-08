package com.example.musicplatform.dto.RedisStats;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SongStats {
    private Long id;
    private Long newPlayCount;
    private Long newFavouriteCount;
    private Long newCommentCount;
    private double newHotScore;
}
