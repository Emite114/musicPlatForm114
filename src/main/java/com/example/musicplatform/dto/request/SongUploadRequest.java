package com.example.musicplatform.dto.request;

import lombok.Data;

@Data
public class SongUploadRequest {
    //todo swagger
    private String songName;
    private String songArtist;
    private String audioUrl;
    private String lrcUrl;
    private String avatarUrl;
}
