package com.example.musicplatform.repository;


import com.example.musicplatform.entity.SongComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongCommentRepository extends JpaRepository<SongComment, Long> {
    Page<SongComment> findBySongIdAndParentIdAndIsDelete(Long songId, Long parentId, Pageable pageable, Boolean isDelete);//查主评论
    Page<SongComment> findByParentIdAndIsDelete(Long parentId,Pageable pageable,Boolean isDelete);
    @Modifying
    @Query("UPDATE SongComment sc SET sc.likeCount=sc.likeCount+1 WHERE sc.id=:id ")
    void increaseLikeCount(@Param("id") Long id);
    @Modifying
    @Query("UPDATE SongComment pc Set pc.likeCount=pc.likeCount-1 WHERE pc.id=:id")
    void decreaseLikeCount(@Param("id") Long id);
    //查找主评论的回复数
    Long countByParentIdAndIsDelete(Long parentId,Boolean isDelete);
}
