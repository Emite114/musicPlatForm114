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
    public SongDetailsResponse(Song song) {
        this.id = song.getId();
        this.songArtist = song.getSongArtist();
        this.audioUrl = song.getAudioUrl();
        this.lrcUrl = song.getLrcUrl();
        this.playCount = song.getPlayCount();
        if (song.getAvatarUrl()!=null){
            this.avatarUrl = song.getAvatarUrl();}
    }
}
