package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media,Long> {
    List<Media> findAllByUserId(Long userId);
    Optional<Media> findById(Long mediaId);

}
