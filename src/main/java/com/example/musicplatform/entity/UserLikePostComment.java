package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user_like_post_comment",comment = "帖子点赞表")
@NoArgsConstructor
@AllArgsConstructor
public class UserLikePostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_comment_id", nullable = false,unique = true)
    private Long postCommentId;
    @Column(name="userid",nullable = false,unique = true)
    private Long userId;
    @Column(name = "create_time",nullable = false)
    private LocalDateTime createTime;
}
