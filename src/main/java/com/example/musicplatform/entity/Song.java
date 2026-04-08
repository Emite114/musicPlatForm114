package com.example.musicplatform.entity;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "is_delete")
    private Boolean isDelete = false;
    @Column(name = "song_name",nullable = false)
    private String songName;
    @Column(name = "song_artist",nullable = false)
    private String songArtist;
    @Column(name = "Audio_url",nullable = false)
    private String audioUrl;
    @Column(name = "lrc_url",nullable = true)
    private String lrcUrl = null;
    @Column(name = "upload_by",nullable = true)
    private Long uploadBy;//userId
    @Column(name = "play_count",nullable = false)
    private Long playCount = 0L;
    @Column(name = "avatar_url")
    private String avatarUrl ;
    @Column(name = "comment_count")
    private Long commentCount = 0L;
    @Column(name = "favourite_count")
    private Long favouriteCount = 0L;
    @Column(name = "creat_at")
    private LocalDateTime createdAt;
    @Column(name="update_at")
    private LocalDateTime updatedAt;
    @Column(name="hot_score")
    private double hotScore = 0.0;

}
