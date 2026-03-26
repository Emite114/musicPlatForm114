package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.PostCommentCreateRequest;
import com.example.musicplatform.dto.response.PostCommentResponse;
import com.example.musicplatform.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCommentService {
    void createComment(PostCommentCreateRequest comment);
    boolean toggleLike(Long id);
    Page<PostCommentResponse> pagePostParentComments(Long postId,int page, int size,String sort);
    Page<PostCommentResponse> pagePostChildrenComments(Long parentId,int page, int size,String sort);

}
