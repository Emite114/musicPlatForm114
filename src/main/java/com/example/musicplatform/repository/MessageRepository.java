package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findBySpeakingUserIdAndReceiveUserId(Long speakingUserId, Long receiveUserId);

    Page<Message> findByConversationIdOrderByCreateDate(Long conversationId, Pageable pageable);
}
