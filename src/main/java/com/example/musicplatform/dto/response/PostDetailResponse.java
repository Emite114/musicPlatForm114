package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data

public class PostDetailResponse {
    private String title;
    private String content;
    private String username;
    private Long postId;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String userAvatarUrl;
    private List<String> mediaUrlList;

    public PostDetailResponse(Post post) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.postId = post.getId();
        this.createdDate = post.getCreateTime();
        this.updatedDate = post.getUpdateTime();

    }
}
