package com.example.musicplatform.repository;

import com.example.musicplatform.entity.UserLikeSongComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLikeSongCommentRepository extends JpaRepository<UserLikeSongComment,Long> {
    Optional<UserLikeSongComment> findBySongCommentIdAndUserId(Long songCommentId, Long userId);
    void deleteBySongCommentIdAndUserId(Long songCommentId, Long userId);
}
