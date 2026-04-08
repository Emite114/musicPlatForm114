package com.example.musicplatform.service.impl;

import com.example.musicplatform.entity.*;
import com.example.musicplatform.repository.*;
import com.example.musicplatform.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    SongRepository songRepository;
    @Autowired
    PostCommentRepository postCommentRepository;
    @Autowired
    SongCommentRepository songCommentRepository;
    @Autowired
    UserRoleRepository userRoleRepository;

    @Transactional
    @Override
    public void banPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(()->new RuntimeException("找不到帖子"));
        post.setStatus(0);
        postRepository.save(post);
    }

    @Transactional
    @Override
    public void banUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(()->new RuntimeException("找不到用户"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void banSong(Long id) {
        Song song = songRepository.findById(id).orElseThrow(()->new RuntimeException("找不到歌曲"));
        song.setIsDelete(true);
        songRepository.save(song);
    }

    @Transactional
    @Override
    public void banPostComment(Long id) {
        PostComment postComment =  postCommentRepository.findById(id).orElseThrow(()->new RuntimeException("找不到帖子评论"));
        postComment.setIsDelete(true);
        postCommentRepository.save(postComment);
    }

    @Transactional
    @Override
    public void banSongComment(Long id) {
        SongComment songComment = songCommentRepository.findById(id).orElseThrow(()->new RuntimeException("找不到歌曲评论"));
        songComment.setIsDelete(true);
        songCommentRepository.save(songComment);
    }

    @Override
    public void addAdmin(Long userId) {
        userRepository.findById(userId).orElseThrow(()->new RuntimeException("找不到用户"));
        UserRole userRole = new UserRole();
        if(userRoleRepository.findByUserIdAndRoleId(userId,2L).isPresent()){
            throw new RuntimeException("该用户已经是管理员");
        }
        userRole.setUserId(userId);
        userRole.setRoleId(2L);
        userRoleRepository.save(userRole);
    }
}
