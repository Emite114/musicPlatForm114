package com.example.musicplatform.dto.response;

import lombok.Data;

@Data
public class PostCommentResponse {
    private Long Id;//converter
    private Long parentId;//converter
    private Long replyToUserId;//converter
    private String replyToUserName;
    private Long likeCount;//converter
    private String content;//converter
    private Long userId;//converter
    private String userName;
    private String userAvatar="/default/defaultUserAvatar.png";
    private String createTime;//converter
    private Long countOfChildren;



}
