package com.example.musicplatform.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationListResponse {
    private Long id;//converter
    private Long talkingToUserId;
    private String talkingToUserName;
    private String talkingToUserAvatar="/default/defaultUserAvatar.png";
    private String userAvatar="/default/defaultUserAvatar.png";
    private Long lastMessageSenderId;//converter
    private String lastMessageSenderName;
    private String lastMessageContent;//converter
    private Long unreadCount;
    private LocalDateTime lastTime;//converter
}
