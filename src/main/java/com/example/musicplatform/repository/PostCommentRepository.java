package com.example.musicplatform.repository;

import com.example.musicplatform.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
//    Page<PostComment> findByPostIdAndParentIdAndIsDeleteOrderByLikeCountDescCreateTimeDesc(Long postId,Long parentId,Pageable pageable,Boolean isDelete);//点赞排序
//    Page<PostComment> findByPostIdAndParentIdAndIsDeleteOrderByCreateTimeDescLikeCountDesc(Long postId,Long parentId,Pageable pageable,Boolean isDelete);//最新排序
    Page<PostComment> findByPostIdAndParentIdAndIsDelete(Long postId,Long parentId,Pageable pageable,Boolean isDelete);//查主评论
    Page<PostComment> findByParentIdAndIsDelete(Long parentId,Pageable pageable,Boolean isDelete);
    @Modifying
    @Query("UPDATE PostComment pc SET pc.likeCount=pc.likeCount+1 WHERE pc.id=:id ")
    void increaseLikeCount(@Param("id") Long id);
    @Modifying
    @Query("UPDATE PostComment pc Set pc.likeCount=pc.likeCount-1 WHERE pc.id=:id")
    void decreaseLikeCount(@Param("id") Long id);
    Long countByParentIdAndIsDelete(Long parentId,Boolean isDelete);
}
