package com.example.musicplatform.dto.request;

import lombok.Data;

@Data
public class PostCommentCreateRequest {
    private Long postId;
    private String content;
    private Long parentId;
    private Long replyToUserId;
}
