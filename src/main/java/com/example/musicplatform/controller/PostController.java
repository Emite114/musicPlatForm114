package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.dto.request.PostCreateRequest;
import com.example.musicplatform.dto.response.PostDetailResponse;
import com.example.musicplatform.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping("/post/create")
    public Response<?> createPost(@RequestBody PostCreateRequest request) {
        try {
            postService.createPost(request);
            return Response.success(null,"发帖成功");
        }catch (Exception e) {return Response.error(null,e.getMessage());}
    }
    @GetMapping("/post/getPost/{id}")
    public Response<?> getPost(@PathVariable Long id) {
        try {
            PostDetailResponse postDetailResponse = postService.getPostById(id);
        return Response.success(postDetailResponse,"查找成功");
        }catch (Exception e){ return  Response.error(null,e.getMessage()); }
    }
    @PostMapping("/post/like/{id}")
    public Response<?> like(@PathVariable Long id) {
        try {
            postService.toggleLike(id);
            return Response.success(null, "操作成功");
        }catch (Exception e){ return Response.error(null,e.getMessage()+"操作失败"); }
    }
    @GetMapping("/post/page")
    public Response<?> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Response.success(postService.page(keyword, page, size), "查询成功");
    }

}
