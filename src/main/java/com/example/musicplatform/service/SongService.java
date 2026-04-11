package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.SongCommentCreateRequest;
import com.example.musicplatform.dto.request.SongUploadRequest;
import com.example.musicplatform.dto.response.PostSimpleDTO;
import com.example.musicplatform.dto.response.SongCommentResponse;
import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.dto.response.SongSimpleDTO;
import com.example.musicplatform.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongService {
    void uploadSong(SongUploadRequest song);
    SongDetailsResponse getSongDetails(@Param("id") Long id);
    Page<SongSimpleDTO> page(String keyword, int page, int pageSize,String sort);
    boolean toggleFavourite(Long songId);
    Page<SongSimpleDTO> getUserOwnFavouriteSongs(String keyword, int page, int size,String sort);
    Page<SongSimpleDTO> getOnesFavouriteSongList(Long id,String keyword, int page, int size,String sort);
    List<Long> getOnesFavouriteSongIdList(Long id,int page,int pageSize,String sort);
    Page<SongSimpleDTO> getOnesSharedSongList(Long id,String keyword, int page, int size,String sort);
}
