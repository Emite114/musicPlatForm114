package com.example.musicplatform.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String id;
    private String content;
    private Long speakingUserId;
    private Long receiveUserId;
    private Long conversationId;
    private LocalDateTime createTime;

}
