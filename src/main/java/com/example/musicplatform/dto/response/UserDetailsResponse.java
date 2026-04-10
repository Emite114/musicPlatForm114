package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.User;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class UserDetailsResponse {
    private Long UserId;
    private String username;
    private String email;
    private String avatarUrl="/default/defaultUserAvatar.png";
    private User.GenderEnum gender;
    private LocalDateTime registerDate;
    private Long followCount;
    private Long fanCount;
    public UserDetailsResponse(User user) {
        this.UserId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        if (user.getAvatarUrl()!=null){
        this.avatarUrl = user.getAvatarUrl();
        }
        this.gender = user.getGender();
        this.registerDate = user.getRegisterTime();
    }

}
