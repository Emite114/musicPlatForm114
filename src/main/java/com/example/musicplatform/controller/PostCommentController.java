package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.dto.request.PostCommentCreateRequest;
import com.example.musicplatform.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class PostCommentController {
    @Autowired
    private PostCommentService postCommentService;
    @PostMapping("/post/comment/create")
    public Response<?> createComment(@RequestBody PostCommentCreateRequest comment) {
        try {
            postCommentService.createComment(comment);
            return Response.success(null,"评论成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 评论失败");
        }
    }
    @PostMapping("/post/comment/like/{id}")
    public Response<?> likeComment(@PathVariable Long id){
        try {
            boolean isLiked = postCommentService.toggleLike(id);
            if(isLiked){
                return Response.success(null,"取消点赞成功");
            }
            return Response.success(null, "点赞成功");
        }
        catch (Exception e){
            return Response.error(null,e.getMessage()+" 操作失败");
        }
    }
    @GetMapping("/post/comment/getParents")
    public Response<?> getParentComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "like") String sort
    ){
        try {
            return Response.success(postCommentService.pagePostParentComments(postId, page, size, sort), "获取父评论成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 获取父评论失败");
        }
    }
    @GetMapping("/post/comment/getChildren")
    public Response<?> getChildrenComments(
            @RequestParam Long parentId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size,
            @RequestParam(defaultValue = "like") String sort
    ){
        try {
            return Response.success(postCommentService.pagePostChildrenComments(parentId,page,size,sort),"获取子评论成功");

        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 获取子评论失败");
        }
    }
    @PutMapping("/post/comment/delete/{id}")
    public Response<?> deleteComment(@PathVariable Long id){
        try {
            postCommentService.deleteComment(id);
            return Response.success(null,"删除成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+"删除失败");
        }
    }
}
