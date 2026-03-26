package com.example.musicplatform.repository;

import com.example.musicplatform.entity.UserLikePostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserLikePostCommentRepository extends JpaRepository<UserLikePostComment, Long> {
    Optional<UserLikePostComment> findByPostCommentIdAndUserId(Long postCommentId, Long userId);
    void deleteByPostCommentIdAndUserId(Long postCommentId, Long userId);
}
