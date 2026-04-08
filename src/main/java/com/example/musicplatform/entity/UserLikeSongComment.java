package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user_like_song_comment")
public class UserLikeSongComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "song_comment_id", nullable = false,unique = false)
    private Long songCommentId;
    @Column(name="userid",nullable = false,unique = false)
    private Long userId;
    @Column(name = "create_time",nullable = false)
    private LocalDateTime createTime;
}
