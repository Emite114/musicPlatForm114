package com.example.musicplatform.repository;

import com.example.musicplatform.entity.UserLikePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLikePostRepository extends JpaRepository<UserLikePost, Long> {
     Optional<UserLikePost> findByPostIdAndUserId(Long postId, Long userId);

    int countByPostId(Long postId);
}
