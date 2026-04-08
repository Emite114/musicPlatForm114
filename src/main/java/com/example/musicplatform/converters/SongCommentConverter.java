package com.example.musicplatform.converters;

import com.example.musicplatform.dto.request.SongCommentCreateRequest;
import com.example.musicplatform.dto.response.SongCommentResponse;
import com.example.musicplatform.entity.SongComment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SongCommentConverter {
    SongComment toEntity(SongCommentCreateRequest request);
    SongCommentResponse toResponse(SongComment entity);

}
