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
@DynamicInsert
@DynamicUpdate
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "songName",nullable = false)
    String songName;
    @Column(name = "songArtist",nullable = false)
    String songArtist;
    @Column(name = "AudioUrl",nullable = false)
    String audioUrl;
    @Column(name = "lrcUrl",nullable = true)
    String lrcUrl = null;
    @Column(name = "uploadBy",nullable = true)
    Long uploadBy;//userId
    @Column(name = "playCount",nullable = false)
    Long playCount = 0L;
    @Column(name = "avatarUrl")
    String avatarUrl ;
    @Column(name = "creatAt")
    LocalDateTime createdAt;
    @Column(name="updateAt")
    LocalDateTime updatedAt;

}
