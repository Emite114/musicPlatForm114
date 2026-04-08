package com.example.musicplatform.converters;

import com.example.musicplatform.dto.request.SendMessageRequest;
import com.example.musicplatform.dto.response.CreateMessageResponse;
import com.example.musicplatform.dto.response.MessageResponse;
import com.example.musicplatform.entity.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageConverter {
    Message requestToEntity(SendMessageRequest request);
    CreateMessageResponse entityToCreateMessageResponse(Message message);
    MessageResponse entityToMessageResponse(Message message);
}
