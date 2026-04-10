package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
//    List<Message> findBySpeakingUserIdAndReceiveUserId(Long speakingUserId, Long receiveUserId);
    boolean existsBySpeakingUserIdAndReceiveUserId(Long speakingUserId, Long receiveUserId);

    Page<Message> findByConversationIdOrderByCreateTime(Long conversationId, Pageable pageable);
}
