package com.example.musicplatform.service.impl;

import com.example.musicplatform.config.CustomUserDetails;
import com.example.musicplatform.converters.UserConverter;
import com.example.musicplatform.dto.request.LoginRequest;
import com.example.musicplatform.dto.response.OnesUserDetail;
import com.example.musicplatform.dto.response.UserDetailsResponse;
import com.example.musicplatform.dto.response.UserSimpleDTO;
import com.example.musicplatform.entity.Follow;
import com.example.musicplatform.entity.Role;
import com.example.musicplatform.entity.User;
import com.example.musicplatform.entity.UserRole;
import com.example.musicplatform.repository.*;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Objects;
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
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

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

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("邮箱已使用");
        }
        if (!isValidEmailCommons(user.getEmail())) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
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
    public String login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getAccount(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

        if (userPrincipal == null) {
            throw new RuntimeException("意外的错误,用户未登录");
        }
        Long userId  = userPrincipal.getUserId();
        if (!Objects.requireNonNull(userRepository.findById(userId).orElse(null)).getIsActive()) {
            throw new RuntimeException("用户已被封禁,联系管理员解封");
        }
        return jwtUtil.generateToken(
                userPrincipal.getUserId(),
                userPrincipal.getUsername());
    }

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
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return userPage.map(user -> {
            UserSimpleDTO dto = new UserSimpleDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            if (user.getAvatarUrl()!=null) {
            dto.setAvatarUrl(user.getAvatarUrl());}
            dto.setPostCount(postRepository.countByUserId(user.getId()));
            dto.setFanCount(user.getFanCount());
            dto.setIfIsFollowed(followRepository.findByUserIdAndFollowUserId(currentUserId, user.getId()).isPresent());
            return dto;
        });
    }

    @Transactional
    @Override
    public boolean toggleFollow(Long followUserId) {
        if(userRepository.findById(followUserId).isEmpty()) throw new RuntimeException("找不到要关注的用户");
        Long userId = SecurityUtils.getCurrentUserId();
        if(Objects.equals(userId, followUserId))throw new RuntimeException("不能关注自己");
        if(followRepository.findByUserIdAndFollowUserId(userId, followUserId).isEmpty()) {
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            follow.setCreateDate(LocalDateTime.now());
            followRepository.save(follow);
            userRepository.increaseFollowCountByUserId(userId);
            userRepository.increaseFanCountByUserId(followUserId);
            return false;
        }
        if(followRepository.findByUserIdAndFollowUserId(userId, followUserId).isPresent()) {
            followRepository.delete(followRepository.findByUserIdAndFollowUserId(userId, followUserId).get());
            userRepository.decreaseFollowCountByUserId(userId);
            userRepository.decreaseFanCountByUserId(followUserId);
            return true;
        }
        else throw new RuntimeException("意外的错误 ");
    }

    Page<UserSimpleDTO> UserPageTODTO(Page<User> userPage) {
        return userPage.map(user->{
            UserSimpleDTO dto = userConverter.userToUserSimpleDTO(user);
            dto.setPostCount(postRepository.countByUserId(user.getId()));
            dto.setIfIsFollowed(true);
            if (user.getAvatarUrl()!=null) {
                dto.setAvatarUrl(user.getAvatarUrl());
            }
            return dto;
        });
    }


    @Override
    public Page<UserSimpleDTO> getOwnFollowList(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Long  currentUserId = SecurityUtils.getCurrentUserId();
        Page<User> userPage = userRepository.findFollows(currentUserId, pageable);
        return UserPageTODTO(userPage);
    }

    @Override
    public Page<UserSimpleDTO> getOwnFanList(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Page<User> userPage = userRepository.findFans(currentUserId, pageable);
        return UserPageTODTO(userPage);
    }

    @Override
    public OnesUserDetail getOnesUserDetail(Long id) {
        User user = userRepository.findById(id).orElseThrow(()->new RuntimeException("找不到用户"));
        OnesUserDetail detail = userConverter.userToOnesUserDetail(user);
        if (user.getAvatarUrl()!=null) {
            detail.setAvatarUrl(user.getAvatarUrl());
        }
        return detail;
    }
}