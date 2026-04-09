package com.example.musicplatform.service.impl;

import com.example.musicplatform.converters.SongCommentConverter;
import com.example.musicplatform.dto.request.SongCommentCreateRequest;

import com.example.musicplatform.dto.response.SongCommentResponse;

import com.example.musicplatform.entity.Song;
import com.example.musicplatform.entity.SongComment;
import com.example.musicplatform.entity.UserLikeSongComment;
import com.example.musicplatform.repository.SongCommentRepository;
import com.example.musicplatform.repository.SongRepository;
import com.example.musicplatform.repository.UserLikeSongCommentRepository;
import com.example.musicplatform.repository.UserRepository;
import com.example.musicplatform.service.SongCommentService;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SongCommentServiceImpl implements SongCommentService {
    @Autowired
    private SongCommentRepository songCommentRepository;
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SongCommentConverter songCommentConverter;
    @Autowired
    private UserLikeSongCommentRepository userLikeSongCommentRepository;
    @Autowired
    private RedisConnectionChecker redisConnectionChecker;
    @Autowired
    private SongStatsService songStatsService;



    @Transactional
    @Override
    public void createComment(SongCommentCreateRequest songCommentCreateRequest) {
        if (songCommentCreateRequest.getReplyToUserId() != null) {
            if (userRepository.findById(songCommentCreateRequest.getReplyToUserId()).isEmpty()) {
                throw new RuntimeException("找不到回复的用户");
            }
        }
        if (songRepository.findById(songCommentCreateRequest.getSongId()).isEmpty()) {
            throw new RuntimeException("找不到被评论的歌曲");
        }
        if (songCommentCreateRequest.getParentId() == null) {
            songCommentCreateRequest.setParentId(0L);
        }
        if (songCommentCreateRequest.getParentId() != 0L) {
            if (songCommentRepository.findById(songCommentCreateRequest.getParentId()).isEmpty()) {
                throw new RuntimeException("找不到主评论");
            }
            if (songCommentCreateRequest.getReplyToUserId() != null) {
                userRepository.findById(songCommentCreateRequest.getReplyToUserId()).orElseThrow(() -> new RuntimeException("找不到回复的用户"));
            }
            SongComment comment = songCommentConverter.toEntity(songCommentCreateRequest);
            comment.setUserId(SecurityUtils.getCurrentUserId());
            comment.setCreateTime(LocalDateTime.now());
            songCommentRepository.save(comment);
            songRepository.increaseCommentCountBySongId(comment.getSongId());
        }
        if (songCommentCreateRequest.getParentId() == 0L) {
            SongComment comment = songCommentConverter.toEntity(songCommentCreateRequest);
            comment.setUserId(SecurityUtils.getCurrentUserId());
            comment.setCreateTime(LocalDateTime.now());
            songCommentRepository.save(comment);
            if(redisConnectionChecker.isRedisConnected()){
                songStatsService.increaseSongCommentCount(comment.getSongId());
                return;
            }
            LogUtil.redisFailLog();
            songRepository.increaseCommentCountBySongId(comment.getSongId());
            Song song = songRepository.findById(comment.getSongId()).orElseThrow(()->new RuntimeException("意外的错误"));
            double hotScore = CalculateUtil.calculateSongHotScore(song.getFavouriteCount(),song.getCommentCount(),song.getPlayCount(),song.getCreateTime());
            songRepository.updateHotScore(comment.getSongId(), hotScore);
        }
    }

    @Override
    @Transactional
    public Page<SongCommentResponse> pageSongParentComments(Long songId, int page, int size, String sort) {
        if (songRepository.findById(songId).isEmpty()) {
            throw new RuntimeException("找不到歌曲");
        }
        Pageable pageable = PageableUtil.initializePageable(page, size, sort);


        Page<SongComment> songComments = songCommentRepository.findBySongIdAndParentIdAndIsDelete(songId, 0L, pageable, false);
        return songComments.map(songComment -> {
            SongCommentResponse sc = songCommentConverter.toResponse(songComment);
            sc.setCountOfChildren(songCommentRepository.countByParentIdAndIsDelete(sc.getId(), false));
            userRepository.findById(songComment.getUserId()).ifPresent(user -> {
                sc.setUserAvatar(user.getAvatarUrl());
                sc.setUserName(user.getUsername());
            });
            return sc;
        });
    }

    @Override
    public Page<SongCommentResponse> pageSongChildrenComments(Long parentId, int page, int size, String sort) {
        if(songCommentRepository.findById(parentId).isEmpty()){
            throw new RuntimeException("找不到父评论");
        }
        Pageable pageable = PageableUtil.initializePageable(page, size, sort);
        Page<SongComment> commentChildren = songCommentRepository.findByParentIdAndIsDelete(parentId,pageable,false);
        return commentChildren.map(songComment -> {
            SongCommentResponse cmp = songCommentConverter.toResponse(songComment);
            userRepository.findById(songComment.getReplyToUserId()).ifPresent(user -> cmp.setReplyToUserName(user.getUsername()));
            userRepository.findById(songComment.getUserId()).ifPresent(user -> {
                    cmp.setUserAvatar(user.getAvatarUrl());
                    cmp.setUserName(user.getUsername());
            });
            return cmp;
        });
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId) {
        SongComment songComment = songCommentRepository.findById(commentId).orElse(null);
        if (songComment != null) {
            if(songComment.getUserId().equals(SecurityUtils.getCurrentUserId())) {
                if (songComment.getIsDelete()==false){
                songComment.setIsDelete(true);
                }else {
                    throw new RuntimeException("该评论已被删除");
                }
            }else {throw new RuntimeException("用户id不匹配");}
        }else {throw new RuntimeException("找不到该评论");}
        songCommentRepository.save(songComment);
        songRepository.findById(songComment.getSongId()).ifPresent(song -> {
            if (redisConnectionChecker.isRedisConnected()) {
                songStatsService.decreaseSongCommentCount(song.getId());
                return;
            }
            LogUtil.redisFailLog();
            songRepository.decreaseCommentCountBySongId(song.getId());
            double hotScore = CalculateUtil.calculateSongHotScore(song.getFavouriteCount(),song.getCommentCount()-1,song.getPlayCount(),song.getCreateTime());
            songRepository.updateHotScore(song.getId(), hotScore);
        });
    }



    @Transactional
    @Override
    public boolean toggleLike(Long songCommentId) {
        if(songCommentRepository.findById(songCommentId).isPresent()){
            Long userId =SecurityUtils.getCurrentUserId();
            if(userLikeSongCommentRepository.findBySongCommentIdAndUserId(songCommentId, userId).isPresent()){
                userLikeSongCommentRepository.deleteBySongCommentIdAndUserId(songCommentId, userId);
                songCommentRepository.decreaseLikeCount(songCommentId);
                return true;
            }else {
                UserLikeSongComment userLikeSongComment = new UserLikeSongComment();
                userLikeSongComment.setSongCommentId(songCommentId);
                userLikeSongComment.setUserId(userId);
                userLikeSongComment.setCreateTime(LocalDateTime.now());
                userLikeSongCommentRepository.save(userLikeSongComment);
                songCommentRepository.increaseLikeCount(songCommentId);
                return false;
            }
        }else {throw new RuntimeException("该评论不存在");}
    }


}
