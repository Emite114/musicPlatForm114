package com.example.musicplatform.config;

import com.example.musicplatform.entity.User;
import com.example.musicplatform.entity.UserRole;
import com.example.musicplatform.repository.RoleRepository;
import com.example.musicplatform.repository.UserRepository;
import com.example.musicplatform.repository.UserRoleRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public CustomUserDetails loadUserByUsername( @NonNull String username) throws UsernameNotFoundException {

        User user;
        if (EmailValidator.getInstance().isValid(username)) {
            // 如果是邮箱，则按邮箱查找
            user = userRepository.findByEmail(username).orElse(null);
        } else {
            // 如果不是邮箱，则按用户名查找
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        Long userId  = user.getId();
        List<UserRole> userRoleList = userRoleRepository.findByUserId(userId);
        List<Long> roleIdList = userRoleList.stream()
                .map(UserRole::getRoleId)
                .toList();

        List<String> roleNameList = roleRepository.findRoleNamesByIds(roleIdList);

        List<GrantedAuthority> authorities = roleNameList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                authorities,
                user.getId(),
                user.getEmail()
        );
    }
}
