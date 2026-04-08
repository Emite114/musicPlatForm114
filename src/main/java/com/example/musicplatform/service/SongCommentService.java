package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.SongCommentCreateRequest;

import com.example.musicplatform.dto.response.SongCommentResponse;
import org.springframework.data.domain.Page;

public interface SongCommentService {
    void createComment(SongCommentCreateRequest songCommentCreateRequest);

    Page<SongCommentResponse> pageSongParentComments(Long songId, int page, int size, String sort);
    Page<SongCommentResponse> pageSongChildrenComments(Long parentId,int page, int size,String sort);
    void deleteComment(Long songId);
    boolean toggleLike(Long id);
}
