package com.example.musicplatform.service.impl;

import com.example.musicplatform.dto.request.PostCreateRequest;
import com.example.musicplatform.dto.response.PostDetailResponse;
import com.example.musicplatform.dto.response.PostSimpleDTO;
import com.example.musicplatform.entity.*;
import com.example.musicplatform.repository.*;
import com.example.musicplatform.service.PostService;
import com.example.musicplatform.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostMediaRepository postMediaRepository;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserLikePostRepository userLikePostRepository;

    @Override
    public void createPost(PostCreateRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUserId(SecurityUtils.getCurrentUserId());
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        postRepository.save(post);

        List<Long> mediaIdList = request.getMediaIdList();
        for (Long mediaId : mediaIdList) {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new RuntimeException("资源不存在"));

            // 校验是不是当前用户的资源
//            if (!media.getUserId().equals(SecurityUtils.getCurrentUserId())) {
//                throw new RuntimeException("不能使用他人的资源");
//            }

            PostMedia pm = new PostMedia();
            pm.setPostId(post.getId());
            pm.setMediaId(mediaId);

            postMediaRepository.save(pm);
        }




    }

    public PostDetailResponse getPostById(@PathVariable Long id) {
        Optional<Post> optional = postRepository.findById(id);
        Post post = optional.orElseThrow(() -> new RuntimeException("文章不存在"));
        PostDetailResponse postDetailResponse = new PostDetailResponse(post);
        Optional<User> optionalUser = userRepository.findById(post.getUserId());
        User user = optionalUser.orElse(null);
        if (user == null) {
            user=new User();
            user.setUsername("已注销用户");
        }
        postDetailResponse.setUsername(user.getUsername());
        postDetailResponse.setUserAvatarUrl(user.getAvatarUrl());
        List<PostMedia> postMediaList = postMediaRepository.findAllByPostId(post.getId());
        if (postMediaList == null) {
            postDetailResponse.setMediaUrlList(null);
        }
        else {
        List<String> mediaUrlList = new ArrayList<>();
        for (PostMedia postMedia : postMediaList) {
            Long mediaId = postMedia.getMediaId();
            Optional<Media> optionalMedia = mediaRepository.findById(mediaId);
            optionalMedia.ifPresent(media -> mediaUrlList.add(media.getUrl()));
        }
        postDetailResponse.setMediaUrlList(mediaUrlList);
        }

        return postDetailResponse;
    }

    //模糊查找帖子
    @Override
    public Page<PostSimpleDTO> page(String keyword, int page, int size) {
        if (keyword == null || keyword.length() < 2) {
            throw new RuntimeException("关键词至少2个字符");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.search(keyword, pageable);
        return postPage.map(post -> {
            PostSimpleDTO dto = new PostSimpleDTO();
            dto.setId(post.getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            List<PostMedia> relations = postMediaRepository.findAllByPostId(post.getId());
            for (PostMedia pm : relations) {
                Media media = mediaRepository.findById(pm.getMediaId()).orElse(null);
                if (media != null && media.getMediaType() == Media.MediaType.image) {
                    dto.setCoverUrl("/media/" + media.getUrl());
                    break;
                }
            }

            return dto;
        });
    }

    //查询用户所有帖子
    @Override
    public Page<PostSimpleDTO> getUserPosts(String username, int page, int size) {
        // 1️⃣ 查用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2️⃣ 分页查帖子
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByUserId(user.getId(), pageable);

        // 3️⃣ 转 DTO
        return postPage.map(post -> {
            PostSimpleDTO dto = new PostSimpleDTO();
            dto.setId(post.getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            return dto;
        });
    }
    @Transactional
    public void toggleLike(Long postId) {

        Long userId = SecurityUtils.getCurrentUserId();

        // 1️⃣ 判断是否已点赞
        Optional<UserLikePost> existing = userLikePostRepository
                .findByPostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            // 已点赞 → 取消点赞
            userLikePostRepository.delete(existing.get());

            postRepository.decrementLikeCountByPostId(postId);

        } else {
            //  未点赞 → 点赞
            UserLikePost like = new UserLikePost();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());

            userLikePostRepository.save(like);

            postRepository.incrementLikeCountByPostId(postId);
        }
    }
}
