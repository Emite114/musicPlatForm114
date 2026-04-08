package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUser1IdAndUser2Id(Long user1Id,Long user2Id);

    @Query("UPDATE Conversation c SET c.unreadCount1 = c.unreadCount1+1 WHERE c.user1Id = :user1Id")
    @Modifying
    void increaseUnreadCount1(@Param("user1Id") Long user1Id);

    @Query("UPDATE Conversation c SET c.unreadCount2 = c.unreadCount2+1 where c.user2Id = :user2Id")
    @Modifying
    void increaseUnreadCount2(@Param("user2Id") Long user2Id);

    @Query("SELECT c from Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId ORDER BY c.lastTime DESC ")
    Page<Conversation> pageByUserId(@Param("userId") Long userId, Pageable pageable);
}
