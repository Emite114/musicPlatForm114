package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.PostCreateRequest;
import com.example.musicplatform.dto.response.PostDetailResponse;
import com.example.musicplatform.dto.response.PostSimpleDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;

public interface PostService {
    void createPost(PostCreateRequest request);
    PostDetailResponse getPostById(@PathVariable Long id);
    Page<PostSimpleDTO> page(String keyword, int page, int size,String sort);
    Page<PostSimpleDTO> getUserPosts(Long id, int page, int size);
    boolean toggleLike(Long postId);
    boolean toggleFavourite(Long postId);
    Page<PostSimpleDTO> getUserOwnFavouritePosts(String keyword, int page, int size);
}
