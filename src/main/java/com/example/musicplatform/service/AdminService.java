package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.LoginRequest;
import org.springframework.web.bind.annotation.RequestBody;

public interface AdminService {
    void banUser(Long id);
    void banPost(Long id);
    void banSong(Long id);
    void banPostComment(Long id);
    void banSongComment(Long id);
    void addAdmin(Long userId);
}
