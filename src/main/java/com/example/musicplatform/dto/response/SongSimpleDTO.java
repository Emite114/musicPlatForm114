package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.Song;
import lombok.Data;

@Data
public class SongSimpleDTO {
    private Long Id;//converter
    private String songName;//converter
    private String songArtist;//converter
    private String avatarUrl="/default/defaultSongAvatar.png";//converter
    private Long playCount;//converter
    private Long favouriteCount;//converter
//done
    private boolean ifIsFavourite;
    private Long commentCount;

}
