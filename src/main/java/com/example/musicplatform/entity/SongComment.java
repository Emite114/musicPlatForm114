package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_song_comment")
public class SongComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "user_id")
    Long userId;
    @Column(name = "song_id")
    Long songId;
    @Column(name = "content")
    String content;
    @Column(name = "parent_id")
    Long parentId;
    @Column(name = "reply_to_user_id")
    Long replyToUserId;
    @Column(name = "like_count")
    Long likeCount = 0L;
    @Column(name = "is_delete")
    Boolean isDelete= false;
    @Column(name = "create_time")
    LocalDateTime createTime;
}
