package com.example.musicplatform.service.impl;


import com.example.musicplatform.converters.SongConverter;
import com.example.musicplatform.dto.request.SongUploadRequest;
import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.dto.response.SongSimpleDTO;
import com.example.musicplatform.entity.*;
import com.example.musicplatform.repository.*;
import com.example.musicplatform.service.SongService;
import com.example.musicplatform.service.redisService.RedisConnectionChecker;
import com.example.musicplatform.service.redisService.SongStatsService;
import com.example.musicplatform.util.CalculateUtil;
import com.example.musicplatform.util.LogUtil;
import com.example.musicplatform.util.PageableUtil;
import com.example.musicplatform.util.SecurityUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Autowired
    private UserFavouriteSongRepository userFavouriteSongRepository;
    @Autowired
    private SongConverter songConverter;
    @Autowired
    private RedisConnectionChecker redisConnectionChecker;
    @Autowired
    private SongStatsService songStatsService;


    protected Page<SongSimpleDTO> entityPageToDTOPage(Page<Song> songPage) {
        return songPage.map(song -> {
            SongSimpleDTO dto = songConverter.toDto(song);
            if (song.getAvatarUrl() != null) {
                dto.setAvatarUrl(song.getAvatarUrl());
            }
            return dto;
        });
    }
    @Transactional
    @Override
    public void uploadSong(SongUploadRequest songRequest) {
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
        if(song.getIsDelete()){
            throw new RuntimeException("该歌曲已被封禁");
        }
        SongDetailsResponse details = songConverter.toDetailsResponse(song);
        if(song.getAvatarUrl()!=null){
            details.setAvatarUrl(song.getAvatarUrl());
        }
        if(redisConnectionChecker.isRedisConnected()){
            songStatsService.increaseSongPlayCount(song.getId());
            details.setPlayCount(details.getPlayCount()+1);
            return details;
        }
        LogUtil.redisFailLog();
        songRepository.increasePlayCountBySongId(id);

        double hotScore = CalculateUtil.calculateSongHotScore(song.getFavouriteCount(),song.getCommentCount(),song.getPlayCount()+1,song.getCreatedAt());
        songRepository.updateHotScore(id,hotScore);

        return details;
    }


    @Override
    public Page<SongSimpleDTO> page(String keyword, int page, int pageSize,String sort) {
        Pageable pageable =  PageableUtil.initializePageable(page, pageSize,sort);
        Page<Song> songPage=songRepository.search(keyword, pageable);
        return entityPageToDTOPage(songPage);
    }

    @Transactional
    @Override
    public boolean toggleFavourite(Long songId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if(songRepository.findById(songId).isEmpty()) {
            throw new RuntimeException("找不到该歌曲");
        }
        //没有就收藏
        if(userFavouriteSongRepository.findBySongIdAndUserId(songId,userId).isEmpty()){
            UserFavouriteSong userFavouriteSong = new UserFavouriteSong();
            userFavouriteSong.setSongId(songId);
            userFavouriteSong.setUserId(userId);
            userFavouriteSong.setCreateDate(LocalDateTime.now());
            userFavouriteSongRepository.save(userFavouriteSong);
            if (redisConnectionChecker.isRedisConnected()) {
                songStatsService.increaseSongFavouriteCount(songId);
                return false;
            }
            LogUtil.redisFailLog();
            songRepository.increaseFavouriteCountBySongId(songId);
            Song  song = songRepository.findById(songId).get();
            double hotScore = CalculateUtil.calculateSongHotScore(song.getFavouriteCount(),song.getCommentCount(),song.getPlayCount(),song.getCreatedAt());
            songRepository.updateHotScore(songId,hotScore);
            return false;
        }
        //有就删
        if (userFavouriteSongRepository.findBySongIdAndUserId(songId,userId).isPresent()){
            userFavouriteSongRepository.deleteBySongIdAndUserId(songId,userId);
            if (redisConnectionChecker.isRedisConnected()) {
                songStatsService.decreaseSongFavouriteCount(songId);
                return true;
            }
            LogUtil.redisFailLog();
            songRepository.decreaseFavouriteCountBySongId(songId);
            Song  song = songRepository.findById(songId).get();
            double hotScore = CalculateUtil.calculateSongHotScore(song.getFavouriteCount(),song.getCommentCount(),song.getPlayCount(),song.getCreatedAt());
            songRepository.updateHotScore(songId,hotScore);
            return true;
        }
        throw new RuntimeException("意外的错误");
    }

    @Override
    public Page<SongSimpleDTO> getUserOwnFavouriteSongs(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        }
        Pageable pageable = PageRequest.of(page, size);
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Song> pageSong = userFavouriteSongRepository.searchUserFavouriteSongByKeyword(userId, keyword, pageable);
        return entityPageToDTOPage(pageSong);
    }

}
