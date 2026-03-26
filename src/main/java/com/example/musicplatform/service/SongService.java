package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.UploadSongRequest;
import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.dto.response.SongSimpleDTO;
import com.example.musicplatform.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

public interface SongService {
    void uploadSong(UploadSongRequest song);
    SongDetailsResponse getSongDetails(@Param("id") Long id);
    Page<SongSimpleDTO> page(String keyword, int page, int pageSize);
}
