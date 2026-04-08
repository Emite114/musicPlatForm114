package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.User;
import lombok.Data;

@Data
public class OnesUserDetail {
    private Long id;
    private String userName;
    private String avatarUrl = "/default/defaultUserAvatar.png";
    private Long followCount;
    private Long fanCount;
    private User.GenderEnum gender;
}
