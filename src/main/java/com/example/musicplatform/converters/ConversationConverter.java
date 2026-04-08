package com.example.musicplatform.converters;

import com.example.musicplatform.dto.response.ConversationListResponse;
import com.example.musicplatform.entity.Conversation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationConverter {
    ConversationListResponse entityToResponse(Conversation conversation);
}
