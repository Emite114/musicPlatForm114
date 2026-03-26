package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "tb_post_media",comment = "帖子文件表")
@NoArgsConstructor
@AllArgsConstructor
public class PostMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name ="postId")
    private Long postId;
    @Column(name = "mediaId")
    private Long MediaId;
}
