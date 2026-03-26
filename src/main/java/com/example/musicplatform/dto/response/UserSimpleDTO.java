package com.example.musicplatform.dto.response;

import lombok.Data;

@Data
public class UserSimpleDTO {
    private Long userId;
    private String username;
    private String avatarUrl="/default/defaultUserAvatar.png";
    private int postCount;
}
