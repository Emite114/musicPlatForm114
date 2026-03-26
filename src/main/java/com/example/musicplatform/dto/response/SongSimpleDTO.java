package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.Song;
import lombok.Data;
//todo:换成mapper converter
@Data
public class SongSimpleDTO {
    private Long songId;
    private String songName;
    private String songArtist;
    private String avatarUrl="/default/defaultSongAvatar.png";
    private Long playCount;


    public SongSimpleDTO(Song song) {
        this.songId = song.getId();
        this.songName = song.getSongName();
        this.songArtist = song.getSongArtist();
        this.playCount = song.getPlayCount();
        if (song.getAvatarUrl()!=null) {
            this.avatarUrl = song.getAvatarUrl();
        }
    }


}
