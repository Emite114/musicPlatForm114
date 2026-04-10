package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "speaking_user_id")
    private Long speakingUserId;

    @Column(name = "receive_user_id")
    private Long receiveUserId;

    @Column(name =  "content")
    private String content;

    @Column(name="conversation_id")
    private Long conversationId;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
