package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.dto.request.SongCommentCreateRequest;
import com.example.musicplatform.service.SongCommentService;
import com.example.musicplatform.service.SongService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SongCommentController {
    @Autowired
    private SongCommentService songCommentService;
    @Autowired
    private SongService songService;

    @PostMapping("/song/createComment")
    public Response<?> createComment(@RequestBody @Valid SongCommentCreateRequest request){
        try {
            songCommentService.createComment(request);
            return Response.success(null,"评论成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 评论失败");
        }
    }
    @GetMapping("/song/comment/getParents")
    public Response<?> getParentCommentPage(
            @RequestParam Long songId,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ){
        try {
            return  Response.success(songCommentService.pageSongParentComments(songId,page,size,sort),"获取主评论成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 获取主评论失败");
        }
    }
    @GetMapping("/song/comment/getChildren")
    public Response<?> getChildrenComments(
            @RequestParam Long parentId,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort
    ){
        try {
            return Response.success(songCommentService.pageSongChildrenComments(parentId,page,size,sort),"获取子评论成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 获取子评论失败");
        }
    }
    @PostMapping("/song/comment/like/{id}")
    public Response<?> likeComment(@PathVariable Long id){
        try {
            boolean isLike = songCommentService.toggleLike(id);
            if(isLike){
                return Response.success(null,"取消点赞成功");
            }else {
                return Response.success(null,"点赞成功");
            }
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 点赞失败");
        }
    }
    @PutMapping("/song/comment/delete/{id}")
    public Response<?> deleteComment(@PathVariable Long id){
        try {
            songCommentService.deleteComment(id);
            return Response.success(null,"删除评论成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 删除评论失败");
        }
    }
}
