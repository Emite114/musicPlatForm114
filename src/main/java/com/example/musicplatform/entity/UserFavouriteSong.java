package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user_favourite_song")
public class UserFavouriteSong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name ="song_id")
    private Long songId;
    @Column(name = "create_date")
    private LocalDateTime createDate;
}
