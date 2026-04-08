package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Post;
import com.example.musicplatform.entity.UserFavouritePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserFavouritePostRepository extends JpaRepository<UserFavouritePost,Long> {
    Optional<UserFavouritePost> findByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);


    @Query("SELECT p FROM Post p WHERE p.id IN " +
            "(SELECT ufp.postId FROM UserFavouritePost ufp WHERE ufp.userId = :userId) " +
            "AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)")
    Page<Post> searchUserFavouritePostByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
