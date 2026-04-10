package com.example.musicplatform.dto.response;

import lombok.Data;

@Data
public class UserSimpleDTO {
    private Long Id;//converter
    private String username;//converter
    private String avatarUrl="/default/defaultUserAvatar.png";
    private int postCount;
    private Long fanCount;
    private boolean ifIsFollowed;
    private boolean ifIsMyFan;
}
