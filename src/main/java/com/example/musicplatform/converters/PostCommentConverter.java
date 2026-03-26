package com.example.musicplatform.converters;

import com.example.musicplatform.dto.request.PostCommentCreateRequest;
import com.example.musicplatform.dto.response.PostCommentResponse;
import com.example.musicplatform.entity.PostComment;
import org.mapstruct.Mapper;



@Mapper(componentModel = "spring")
public interface PostCommentConverter {
    PostComment toEntity (PostCommentCreateRequest request);
    PostCommentResponse toDTO (PostComment entity);
}
