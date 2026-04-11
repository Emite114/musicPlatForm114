package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface SongRepository extends JpaRepository<Song, Long> {

    //    @Modifying
//    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
//    void incrementPlayCount(@Param("id") Long id);

    //todo sql语法
    /**
     * 全局搜索歌曲（支持 keyword 为空）
     */
    @Query("SELECT s FROM Song s WHERE (:keyword IS NULL OR " +
            "s.songName LIKE %:keyword% OR " +
            "s.songArtist LIKE %:keyword%) " +
            "AND s.isDelete = false")
    Page<Song> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 搜索用户上传的歌曲（修复版）
     */
    @Query("SELECT s FROM Song s WHERE s.uploadBy = :userId " +
            "AND (:keyword IS NULL OR s.songName LIKE %:keyword% OR s.songArtist LIKE %:keyword%) " +
            "AND s.isDelete = false")
    Page<Song> searchUserSharedSongs(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Song s SET s.commentCount = s.commentCount + 1 WHERE s.id = :songId")
    void increaseCommentCountBySongId(@Param("songId") Long songId);

    @Transactional
    @Modifying
    @Query("UPDATE Song s SET s.commentCount = s.commentCount - 1 WHERE s.id = :songId")
    void decreaseCommentCountBySongId(@Param("songId") Long songId);

    @Transactional
    @Modifying
    @Query("UPDATE Song s SET s.favouriteCount = s.favouriteCount + 1 WHERE s.id = :songId")
    void increaseFavouriteCountBySongId(@Param("songId") Long songId);

    @Transactional
    @Modifying
    @Query("UPDATE Song s SET s.favouriteCount = s.favouriteCount - 1 WHERE s.id = :songId")
    void decreaseFavouriteCountBySongId(@Param("songId") Long songId);

    @Transactional
    @Modifying
    @Query("UPDATE Song s SET s.playCount = s.playCount+1 WHERE s.id = :songId")
    void increasePlayCountBySongId(@Param("songId") Long songId);

    @Transactional
    @Modifying
    @Query("UPDATE Song s SET s.hotScore=:hotScore WHERE s.id= :songId")
    void updateHotScore(@Param("songId") Long songId, @Param("hotScore") double hotScore);
}
