package com.example.musicplatform.dto.response;

import lombok.Data;

@Data
public class PostCommentResponse {
    private Long id;//converter
    private Long parentId;//converter
    private Long replyToUserId;//converter
    private String replyToUserName;
    private Long likeCount;//converter
    private String content;//converter
    private Long userId;//converter
    private String username;
    private String userAvatar="/default/defaultUserAvatar.png";
    private String createTime;//converter
    private Long countOfChildren;

    private boolean ifIsLiked;



}
