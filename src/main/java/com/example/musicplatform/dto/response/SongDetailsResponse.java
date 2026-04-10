package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.Song;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SongDetailsResponse {
    private Long id;
    private String songArtist;
    private String audioUrl;
    private String lrcUrl;
    private String avatarUrl="/default/defaultSongAvatar.png";
    private Long playCount;
    //done
    private Long commentCount;
    private Long favouriteCount;
    private boolean ifIsFavourite;

}
