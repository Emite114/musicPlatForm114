package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.SendMessageRequest;
import com.example.musicplatform.dto.response.ConversationListResponse;
import com.example.musicplatform.dto.response.CreateMessageResponse;
import com.example.musicplatform.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    CreateMessageResponse sendMessage(SendMessageRequest request);
    Page<ConversationListResponse> getConversationList(int page, int size);
    Page<MessageResponse> getMessages(Long conversationId, int page, int size);
    void markAsReadBecauseTheUserIsReadingTheConversation(Long conversationId);
}
