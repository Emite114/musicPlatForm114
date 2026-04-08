package com.example.musicplatform.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateMessageResponse {
    //消息信息
    private Long Id;
    private Long conversationId;
    private Long speakingUserId;
    private Long receiveUserId;
    private String content;
    private LocalDateTime createTime;

    //对话信息
    private String lastMessage;
    private Long lastMessageSenderId;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
