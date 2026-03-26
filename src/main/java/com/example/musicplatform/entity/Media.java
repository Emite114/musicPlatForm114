package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_media",comment = "媒体文件主表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    public enum MediaType{
        audio,image,lrc
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="original_name",nullable = false,length = 100)
    String originalName;

    @Column(name="user_id",nullable = false,length = 60)
    private Long userId;

    @Column(name = "media_type",nullable = false,length = 20)
    private MediaType mediaType;

    @Column(name = "size",nullable = false,length = 60)
    private Long size;

    @Column(name = "create_date",nullable = false)
    private LocalDateTime createDate;

    @Column(name = "url",nullable = false,length = 200)
    private String url;


}
