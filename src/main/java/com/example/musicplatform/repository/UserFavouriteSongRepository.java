package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Song;
import com.example.musicplatform.entity.UserFavouriteSong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserFavouriteSongRepository extends JpaRepository<UserFavouriteSong, Long> {
    Optional<UserFavouriteSong> findBySongIdAndUserId(Long songId, Long userId);
    void deleteBySongIdAndUserId(Long songId, Long userId);
    @Query("SELECT s FROM Song s WHERE s.id IN " +
            "(SELECT ufp.songId FROM UserFavouriteSong ufp WHERE ufp.userId = :userId) " +
            "AND (s.songName LIKE %:keyword% OR s.songArtist LIKE %:keyword%)" +
            "AND (s.isDelete = false )")
    Page<Song> searchUserFavouriteSongByKeyword(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
