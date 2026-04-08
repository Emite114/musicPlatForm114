package com.example.musicplatform.service.impl;

import com.example.musicplatform.entity.UserRole;
import com.example.musicplatform.repository.RoleRepository;
import com.example.musicplatform.repository.UserRepository;
import com.example.musicplatform.repository.UserRoleRepository;
import com.example.musicplatform.service.SseService;
import com.example.musicplatform.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SseServiceImpl implements SseService {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;

    public boolean isAdmin(){
        Long userId = SecurityUtils.getCurrentUserId();
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        for (UserRole userRole : userRoles) {
            if(Objects.requireNonNull(roleRepository.findById(userRole.getRoleId()).orElse(null)).getRoleName().equals("ROLE_ADMIN")){
                return true;
            }
        }
        return false;
    }
}
