package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user_like_post",comment = "帖子点赞表")
@NoArgsConstructor
@AllArgsConstructor
public class UserLikePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "postid", nullable = false,unique = true)
    private Long postId;
    @Column(name="userid",nullable = false,unique = true)
    private Long userId;
    @Column(name = "create_time",nullable = false)
    private LocalDateTime createTime;
}
