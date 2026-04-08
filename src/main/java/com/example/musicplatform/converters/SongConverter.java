package com.example.musicplatform.converters;

import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.dto.response.SongSimpleDTO;
import com.example.musicplatform.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SongConverter {
    @Mapping(target = "avatarUrl",ignore = true)
    SongSimpleDTO toDto(Song entity);
    @Mapping(target = "avatarUrl",ignore = true)
    SongDetailsResponse toDetailsResponse(Song entity);
}
