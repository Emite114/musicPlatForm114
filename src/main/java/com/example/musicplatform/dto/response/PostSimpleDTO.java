package com.example.musicplatform.dto.response;

import lombok.Data;

@Data
public class PostSimpleDTO {
    private Long id;
    private Long userId;
    private String username;
    private String title;
    private String content;
    private String coverUrl="/default/defaultSongAvatar.png";
    private Long likeCount;
}
