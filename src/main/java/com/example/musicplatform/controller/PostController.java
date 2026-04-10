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
            boolean isLike = postService.toggleLike(id);
            if(isLike) {
            return Response.success(null, "取消点赞成功");}
            else {return Response.success(null,"点赞成功");}
        }catch (Exception e){ return Response.error(null,e.getMessage()+"操作失败"); }
    }
    @GetMapping("/post/page")
    public Response<?> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "time")String sort) {
        return Response.success(postService.page(keyword, page, size,sort), "查询成功");
    }
    @PostMapping("/post/favourite/{postId}")
    public Response<?> favourite(@PathVariable Long postId) {
        try {
        boolean isFavourite = postService.toggleFavourite(postId);
        if (isFavourite) {
            return Response.success(null,"取消收藏成功");}
        else{
            return Response.success(null,"收藏成功");
        }
        }catch (Exception e){ return Response.error(null,e.getMessage()+"操作失败"); }
    }
    @GetMapping("post/favourite/userOwn")
    public Response<?> getFavouritePostUserOwn(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20")int size,
            @RequestParam(defaultValue = "time")String sort
    ){
        try {
            return Response.success(postService.getUserOwnFavouritePosts(keyword, page, size,sort),"操作成功");
        } catch (Exception e) {
            return Response.error(null,e.getMessage()+"操作失败");
        }
    }

    @GetMapping("/post/getOnesFavouritePosts")
    public Response<?> getOneFavouritePost(
            @RequestParam Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20")int size,
            @RequestParam(defaultValue = "time")String sort
    ){
        try {
            return Response.success(postService.getOnesFavouritePosts(id,keyword,page,size,sort),"操作成功");
        }catch (Exception e) {
            return Response.error(null,e.getMessage()+" 操作失败");
        }
    }
}
