package com.example.musicplatform.service;


import com.example.musicplatform.dto.response.UserDetailsResponse;
import com.example.musicplatform.dto.response.UserSimpleDTO;
import com.example.musicplatform.entity.User;
import org.springframework.data.domain.Page;

public interface UserService {
    void register (User user);
    void updateUserAvatar(String avatarUrl);
    void updateUserGender(User.GenderEnum gender);
    UserDetailsResponse getUserDetails();
    Page<UserSimpleDTO> searchUser(String search, int page, int pageSize);







}
