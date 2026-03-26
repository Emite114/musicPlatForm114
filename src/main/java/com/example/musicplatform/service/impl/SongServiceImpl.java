package com.example.musicplatform.service.impl;

import com.example.musicplatform.dto.request.UploadSongRequest;
import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.dto.response.SongSimpleDTO;
import com.example.musicplatform.entity.Song;
import com.example.musicplatform.repository.SongRepository;
import com.example.musicplatform.service.SongService;
import com.example.musicplatform.util.SecurityUtils;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SongServiceImpl implements SongService {
    @Autowired
    private SongRepository songRepository;

    @Transactional
    @Override
    public void uploadSong(UploadSongRequest songRequest) {
        Song song = new Song();
        if (songRequest.getSongName() == null){
            throw new RuntimeException("歌曲名不能为空");
        }
        if (songRequest.getAudioUrl() == null){
            throw new RuntimeException("歌曲音源不能为空");
        }
        song.setSongName(songRequest.getSongName());
        String audioExt =songRequest.getAudioUrl().substring(songRequest.getAudioUrl().lastIndexOf("."));
        String lrcExt =songRequest.getLrcUrl().substring(songRequest.getLrcUrl().lastIndexOf("."));
        String avatarExt = "";
        if(songRequest.getAvatarUrl() != null) {
            avatarExt = songRequest.getAvatarUrl().substring(songRequest.getAvatarUrl().lastIndexOf("."));
            if (avatarExt.equals(".jpg")||avatarExt.equals(".png")||avatarExt.equals("jpeg")) {
                song.setAvatarUrl(songRequest.getAvatarUrl());
            }else throw new RuntimeException("歌曲图片只能为jpg,png或jpeg格式");
        }

        if ( audioExt.equals(".mp3")||audioExt.equals(".wav")) {
        song.setAudioUrl(songRequest.getAudioUrl());
        }else {throw new RuntimeException("歌曲音源只能为.mp3或.wav");}
        if (songRequest.getSongArtist()!=null){
        song.setSongArtist(songRequest.getSongArtist());
        }
        if(songRequest.getLrcUrl()!=null){
            if (lrcExt.equals(".lrc")){
                song.setLrcUrl(songRequest.getLrcUrl());}
            else {
                throw new RuntimeException("歌词只能是.lrc格式");
            }
        }
        if(songRequest.getSongArtist()!=null){
            song.setSongArtist(songRequest.getSongArtist());
        }else {song.setSongArtist("N/A");}
        song.setUploadBy(SecurityUtils.getCurrentUserId());
        song.setCreatedAt(LocalDateTime.now());
        song.setUpdatedAt(LocalDateTime.now());
        songRepository.save(song);

    }

    @Override
    @Transactional
    public SongDetailsResponse getSongDetails(Long id) {
        Optional<Song> optional = songRepository.findById(id);
        Song song = optional.orElseThrow(()->new RuntimeException("找不到歌曲"));
        song.setPlayCount(song.getPlayCount()+1);
        return new  SongDetailsResponse(song);
    }


    @Override
    public Page<SongSimpleDTO> page(String keyword, int page, int pageSize) {
        if(keyword==null) {
            throw new RuntimeException("关键字不能为空");
        }
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Song> songPage=songRepository.search(keyword, pageable);
        return songPage.map(SongSimpleDTO::new);
    }
}
