package com.example.musicplatform.dto.request;

import com.example.musicplatform.entity.Song;
import lombok.Data;

@Data
public class UploadSongRequest {
    //todo swagger
    private String songName;
    private String songArtist;
    private String audioUrl;
    private String lrcUrl;
    private String avatarUrl;
}
