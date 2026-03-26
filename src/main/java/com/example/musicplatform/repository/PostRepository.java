package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
   @Query("""
    SELECT p FROM Post p
    WHERE (:keyword IS NULL OR 
           p.title LIKE %:keyword% OR 
           p.content LIKE %:keyword%)
    ORDER BY p.createTime DESC
""")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Post> findByUserId(Long userId, Pageable pageable);
    int countByUserId(Long userId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCountByPostId(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCountByPostId(@Param("id") Long id);
}
