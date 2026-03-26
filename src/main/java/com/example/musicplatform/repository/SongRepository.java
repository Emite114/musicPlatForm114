package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface SongRepository extends JpaRepository<Song, Long> {

    //    @Modifying
//    @Query("UPDATE Song s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
//    void incrementPlayCount(@Param("id") Long id);

    //todo sql语法
    @Query("""
    SELECT s FROM Song s
    WHERE (:keyword IS NULL OR 
           s.songName LIKE %:keyword% OR 
           s.songArtist LIKE %:keyword%)
    ORDER BY s.createdAt DESC
""")
    Page<Song> search(@Param("keyword") String keyword, Pageable pageable);
}
