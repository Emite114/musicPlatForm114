package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.PostCreateRequest;
import com.example.musicplatform.dto.response.PostDetailResponse;
import com.example.musicplatform.dto.response.PostSimpleDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;

public interface PostService {
    void createPost(PostCreateRequest request);
    PostDetailResponse getPostById(@PathVariable Long id);
    Page<PostSimpleDTO> page(String keyword, int page, int size);
    Page<PostSimpleDTO> getUserPosts(String username, int page, int size);
    void toggleLike(Long postId);
}
