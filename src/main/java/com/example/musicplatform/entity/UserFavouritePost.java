package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user_favourite_post")
public class UserFavouritePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name ="post_id")
    private Long postId;
    @Column(name = "create_date")
    private LocalDateTime createDate;
}
