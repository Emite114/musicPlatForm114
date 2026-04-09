package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data

public class PostDetailResponse {
    private String title;
    private String content;
    private Long userId;
    private String username;
    private Long id;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String userAvatarUrl;
    private List<String> mediaUrlList;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private boolean ifIsFollowed;

    public PostDetailResponse(Post post) {
        this.userId = post.getUserId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.id = post.getId();
        this.createdDate = post.getCreateTime();
        this.updatedDate = post.getUpdateTime();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
    }
}
