package com.example.musicplatform.dto.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageEvent {
    private String type;
    private Long conversationId;
    private Long speakingUserId;
    private Long receiveUserId;
    private String content;
    private LocalDateTime createTime;
    private Long unreadCount;
}
