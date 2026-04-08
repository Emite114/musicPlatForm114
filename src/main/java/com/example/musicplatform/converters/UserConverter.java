package com.example.musicplatform.converters;

import com.example.musicplatform.dto.response.OnesUserDetail;
import com.example.musicplatform.dto.response.UserSimpleDTO;
import com.example.musicplatform.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserConverter {
    @Mapping(target = "avatarUrl",ignore = true)
    UserSimpleDTO userToUserSimpleDTO(User user);

    @Mapping(target = "avatarUrl",ignore = true)
    OnesUserDetail userToOnesUserDetail(User user);
}
