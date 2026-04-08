package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.repository.cdi.Eager;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_conversation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id"}),
        indexes = {
                @Index(name = "idx_user1", columnList = "user1_id"),
                @Index(name = "idx_user2", columnList = "user2_id"),
                @Index(name = "idx_last_time", columnList = "last_time")
        })
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user1_id")
    private Long user1Id;
    @Column(name ="unread_count1")
    private Long unreadCount1=0L;
    @Column(name= "user2_id")
    private Long user2Id;
    @Column(name = "unread_count2")
    private Long unreadCount2=0L;
    @Column(name = "last_message_content")
    private String lastMessageContent;
    @Column(name = "last_message_sender_id")
    private Long lastMessageSenderId;
    @Column(name = "last_time")
    private LocalDateTime lastTime;
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
