package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "tb_post",comment = "帖子主表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "bigint unsigned comment '帖子ID（主键）'")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "bigint unsigned comment '发帖用户ID'")
    private Long userId;

    @Column(name = "title", nullable = false, length = 200, columnDefinition = "varchar(200) NOT NULL comment '帖子标题'")
    private String title;

    @Column(name = "content", columnDefinition = "text comment '帖子正文'")
    private String content;

    @Column(name = "create_time", nullable = false, updatable = false, columnDefinition = "datetime NOT NULL DEFAULT CURRENT_TIMESTAMP comment '发布时间'")
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false, columnDefinition = "datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '更新时间'")
    private LocalDateTime updateTime;

    @Column(name = "status", nullable = false, columnDefinition = "tinyint NOT NULL DEFAULT 1 comment '状态：1-正常，0-删除'")
    private Integer status = 1; // 默认状态为1
//todo:redis统计数据延迟落盘
    @Column(name = "view_count")
    private Long viewCount = 0L;
    @Column(name = "like_count",nullable = false)
    private Long likeCount = 0L;
    @Column(name = "comment_count")
    private Long commentCount = 0L;
    @Column(name = "favourite_count")
    private Long favouriteCount = 0L;
    @Column(name="hot_score")
    private double hotScore = 0.0;


}
