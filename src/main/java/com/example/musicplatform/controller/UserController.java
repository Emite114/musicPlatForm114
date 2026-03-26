package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.config.CustomUserDetails;
import com.example.musicplatform.dto.request.LoginRequest;
import com.example.musicplatform.dto.response.UserDetailsResponse;
import com.example.musicplatform.entity.User;
import com.example.musicplatform.service.PostService;
import com.example.musicplatform.service.UserService;
import com.example.musicplatform.service.impl.TokenBlacklistService;
import com.example.musicplatform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController

public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    public UserController(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }


    @PostMapping("/user/register")
    public Response<?> register(@RequestBody User user) {
        try {
            userService.register(user);
            return Response.success(null," 注册成功");
        }catch (IllegalArgumentException e){
            return Response.error(null,e.getMessage()+" 注册失败");
        }
    }

    @PostMapping("/user/login")
    public Response<?> login(@RequestBody LoginRequest  loginRequest) {
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getAccount(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();

            if (userPrincipal == null) {
                throw new RuntimeException("用户未登录");
            }
            String token = jwtUtil.generateToken(
                    userPrincipal.getUserId(),
                    userPrincipal.getUsername());

            return Response.success(token, " 登陆成功喵");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 登录失败喵");
        }
    }
    @Autowired
    private final TokenBlacklistService tokenBlacklistService;
    @PostMapping("/user/logout")
    public Response<?> logout(HttpServletRequest request) {
        try {


            String authHeader = request.getHeader("Authorization");
            // 1. 判断 header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.error(null, "无法从请求头得到jwt");
            }

            // 2. 提取 token
            String token = authHeader.substring(7);

            // 3. 获取剩余时间（关键）
            long remainingTime = jwtUtil.getRemainingTime(token);

            if (remainingTime <= 0) {
                return Response.error(null, "你的token早已经过期了");
            }
            // 4. 加入黑名单
            tokenBlacklistService.addToBlacklist(token, remainingTime);
        }catch (Exception e){
            return Response.error(null,e.getMessage());
        }
        return Response.success(null,"成功登出!");
    }
    @PutMapping("/user/update/avatarUrl")
    public Response<?> updateAvatarUrl(@RequestBody String avatarUrl) {
        try {
        userService.updateUserAvatar(avatarUrl);
        return Response.success(null,"用户头像修改成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage());
        }
    }
    @PutMapping("/user/update/gender")//有三种性别male female unknown
    public Response<?> updateGender(@RequestBody String request) {
        try {
            User.GenderEnum gender = User.GenderEnum.valueOf(request.toLowerCase());
            userService.updateUserGender(gender);
            return Response.success(null,"性别修改成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage());
        }
    }
    @GetMapping("/user/myDetails")
    public Response<?> getMyDetails() {
        try {
            UserDetailsResponse userDetailsResponse = userService.getUserDetails();
            return Response.success(userDetailsResponse,"用户主页信息返回成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 找不到用户信息");
        }
    }
    @Autowired
    PostService postService;
    @GetMapping("/user/main/{username}/posts")
    public Response<?> userPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return Response.success(
                postService.getUserPosts(username, page, size),
                "查询成功"
        );
    }
    @GetMapping("/user/search")
    public Response<?> searchUser(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
        return Response.success(
                userService.searchUser(keyword, page, size),
                "查询成功"
        );}catch (Exception e){
            return Response.error(null,e.getMessage());
        }
    }


}
