package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.SongCommentCreateRequest;
import com.example.musicplatform.dto.request.SongUploadRequest;
import com.example.musicplatform.dto.response.PostSimpleDTO;
import com.example.musicplatform.dto.response.SongCommentResponse;
import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.dto.response.SongSimpleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

public interface SongService {
    void uploadSong(SongUploadRequest song);
    SongDetailsResponse getSongDetails(@Param("id") Long id);
    Page<SongSimpleDTO> page(String keyword, int page, int pageSize);
    boolean toggleFavourite(Long songId);
    Page<SongSimpleDTO> getUserOwnFavouriteSongs(String keyword, int page, int size);

}
