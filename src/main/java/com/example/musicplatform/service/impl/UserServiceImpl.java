package com.example.musicplatform.service.impl;

import com.example.musicplatform.dto.response.UserDetailsResponse;
import com.example.musicplatform.dto.response.UserSimpleDTO;
import com.example.musicplatform.entity.Role;
import com.example.musicplatform.entity.User;
import com.example.musicplatform.entity.UserRole;
import com.example.musicplatform.repository.PostRepository;
import com.example.musicplatform.repository.RoleRepository;
import com.example.musicplatform.repository.UserRepository;
import com.example.musicplatform.repository.UserRoleRepository;
import com.example.musicplatform.service.UserService;
import com.example.musicplatform.util.JwtUtil;
import com.example.musicplatform.util.NumberChecker;
import com.example.musicplatform.util.SecurityUtils;
import jakarta.transaction.Transactional;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    private PostRepository postRepository;

    //密码加密器
//    org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
//            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    //邮箱验证器
    public static boolean isValidEmailCommons(String email) {
        return EmailValidator.getInstance().isValid(email);
    }


    @Override
    @Transactional
    public void register(User user) {
        if (ObjectUtils.isEmpty(user.getEmail())) throw new IllegalArgumentException("邮箱地址不能为空");
        if (ObjectUtils.isEmpty(user.getPassword())) throw new IllegalArgumentException("密码不能为空");
        if (ObjectUtils.isEmpty(user.getUsername())) throw new IllegalArgumentException("用户名不能为空");

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("邮箱已使用");
        }
        if (!isValidEmailCommons(user.getEmail())) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名重复");
        }
        if (EmailValidator.getInstance().isValid(user.getUsername())){
            throw new IllegalArgumentException("用户名不得是邮箱格式");
        }
        if(user.getUsername().length() == 11){
            if(NumberChecker.isAllDigits(user.getUsername())){
            throw new IllegalArgumentException("用户名不得是手机号");
            }
        }
        if (user.getPassword().length() <= 6) {
            throw new IllegalArgumentException("密码长度必须大于6位");
        }

        //默认用户的角色 为user
        Role defaultRole = roleRepository.findByRoleName("ROLE_USER");
        if (defaultRole == null) {
            throw new IllegalStateException("数据库缺少默认角色 ROLE_USER");
        }
        Long defaultRoleId = defaultRole.getId();


        User savedUser = userRepository.save(user);
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRoleId);
        userRoleRepository.save(userRole);
//      user.setPassword(encoder.encode(user.getPassword()));

    }

    //通过邮件查找用户来返回dto对象


    @Override
    @Transactional
    public void updateUserAvatar(String avatarUrl) {
        Long userId =  SecurityUtils.getCurrentUserId();
        Optional<User> optional = userRepository.findById(userId);
        User user = optional.orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatarUrl(avatarUrl);
    }

    @Override
    @Transactional
    public void updateUserGender(User.GenderEnum gender) {
        User user = userRepository.findById(SecurityUtils.getCurrentUserId()).orElseThrow(()-> new RuntimeException("用户不存在"));
        user.setGender(gender);
    }

    @Override
    public UserDetailsResponse getUserDetails() {
        Long userId = SecurityUtils.getCurrentUserId();
        Optional<User> Op = userRepository.findById(userId);
        User user = Op.orElseThrow(()-> new RuntimeException("未找到用户,jwt已过期"));
        return new UserDetailsResponse(user);
    }

    //模糊查询用户
    @Override
    public Page<UserSimpleDTO> searchUser(String keyword, int page, int pageSize) {
        if (keyword == null || keyword.length() < 2) {
            throw new RuntimeException("关键词至少2个字符");
        }
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<User> userPage = userRepository.search(keyword, pageable);

        return userPage.map(user -> {
            UserSimpleDTO dto = new UserSimpleDTO();
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            if (!user.getAvatarUrl().isEmpty()) {
            dto.setAvatarUrl(user.getAvatarUrl());}
            dto.setPostCount(postRepository.countByUserId(user.getId()));
            return dto;
        });

    }
}