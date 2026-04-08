package com.example.musicplatform.entity;

import com.example.musicplatform.dto.request.PostCommentCreateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_post_comment",comment = "帖子主表")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_id",nullable = false)
    private Long postId;
    @Column(name = "user_id",nullable = false)
    private Long userId;
    @Column(name = "content",nullable = false)
    private String content;
    @Column(name="parent_id")
    //todo 树形结构
    private Long parentId;
    @Column(name = "reply_to_user_id")
    private Long replyToUserId;
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @Column(name = "like_count")
    private Long likeCount=0L;
    @Column(name ="is_delete")
    private Boolean isDelete=false;

}
