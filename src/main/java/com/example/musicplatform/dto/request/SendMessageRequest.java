package com.example.musicplatform.dto.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String content;
    private Long receiveUserId;
}
