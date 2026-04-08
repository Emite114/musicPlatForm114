package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PostRepository extends JpaRepository<Post, Long> {
   @Query("SELECT p FROM Post p " +
           "WHERE (:keyword IS NULL OR " +
           "p.title LIKE CONCAT('%', :keyword, '%') OR " +
           "p.content LIKE CONCAT('%', :keyword, '%')) " +
           "AND p.status = 1 " +
           "ORDER BY p.createTime DESC")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Post> findByUserId(Long userId, Pageable pageable);
    int countByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    void decreaseLikeCountByPostId(@Param("postId") Long postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void increaseLikeCountByPostId(@Param("postId") Long postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void increaseCommentCountByPostId(@Param("postId") Long postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId")
    void decreaseCommentCountByPostId(@Param("postId") Long postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.favouriteCount = p.favouriteCount + 1 WHERE p.id = :postId")
    void increaseFavouriteCountByPostId(@Param("postId") Long postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.favouriteCount = p.favouriteCount - 1 WHERE p.id = :postId")
    void decreaseFavouriteCountByPostId(@Param("postId") Long postId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount+1 WHERE p.id=:postId")
    void increaseViewCountByPostId(@Param("postId") Long postId);

}
