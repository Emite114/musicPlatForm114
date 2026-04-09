package com.example.musicplatform.service.impl;

import com.example.musicplatform.dto.request.PostCreateRequest;
import com.example.musicplatform.dto.response.PostDetailResponse;
import com.example.musicplatform.dto.response.PostSimpleDTO;
import com.example.musicplatform.entity.*;
import com.example.musicplatform.repository.*;
import com.example.musicplatform.service.PostService;
import com.example.musicplatform.service.redisService.PostStatsService;
import com.example.musicplatform.service.redisService.RedisConnectionChecker;
import com.example.musicplatform.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @Autowired
    private UserFavouritePostRepository userFavouritePostRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private RedisConnectionChecker redisConnectionChecker;
    @Autowired
    private PostStatsService postStatsService;



    protected Page<PostSimpleDTO> entityPageToDTOPage(Page<Post> postPage) {
        return postPage.map(post -> {
            PostSimpleDTO dto = new PostSimpleDTO();
            dto.setId(post.getId());
            dto.setTitle(post.getTitle());
            dto.setContent(post.getContent());
            dto.setLikeCount(post.getLikeCount());
            dto.setUserId(post.getUserId());
            dto.setUsername(
                    userRepository.findById(post.getUserId())
                    .map(User::getUsername)
                    .orElse("未知用户")
            );

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

            // 资源校验,没必要
//            if (!media.getUserId().equals(SecurityUtils.getCurrentUserId())) {
//                throw new RuntimeException("不能使用他人的资源");
//            }

            PostMedia pm = new PostMedia();
            pm.setPostId(post.getId());
            pm.setMediaId(mediaId);

            postMediaRepository.save(pm);
        }




    }

    @Transactional
    public PostDetailResponse getPostById(@PathVariable Long id) {
        Optional<Post> optional = postRepository.findById(id);
        Post post = optional.orElseThrow(() -> new RuntimeException("文章不存在"));
        if(post.getStatus()==0){
            throw new RuntimeException("帖子已被封禁");
        }
        PostDetailResponse postDetailResponse = new PostDetailResponse(post);
        postDetailResponse.setViewCount(post.getViewCount());
        Long currentUserId = SecurityUtils.getCurrentUserId();
        postDetailResponse.setIfIsFollowed(followRepository.findByUserIdAndFollowUserId(currentUserId, post.getUserId()).isPresent());
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
        //浏览量
        postDetailResponse.setViewCount(postDetailResponse.getViewCount()+1);
        if(redisConnectionChecker.isRedisConnected()){
            postStatsService.increasePostViewCount(post.getId());

        }else {
            LogUtil.redisFailLog();
            postRepository.increaseViewCountByPostId(post.getId());
            double hotScore = CalculateUtil.calculatePostHotScore(post.getViewCount()+1,post.getLikeCount(),post.getCommentCount(),post.getFavouriteCount(),post.getCreateTime());
            postRepository.updateHotScore(post.getId(), hotScore);
        }

        return postDetailResponse;
    }

    //模糊查找帖子
    @Override
    public Page<PostSimpleDTO> page(String keyword, int page, int size,String sort) {

        Pageable pageable = PageableUtil.initializePageable(page, size, sort);
        Page<Post> postPage = postRepository.search(keyword, pageable);
        return entityPageToDTOPage(postPage);
    }

    //查询用户所有帖子
    @Override
    public Page<PostSimpleDTO> getUserPosts(Long id, int page, int size) {
        // 1️⃣ 查用户
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 2️⃣ 分页查帖子
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postPage = postRepository.findByUserId(user.getId(), pageable);

        // 3️⃣ 转 DTO
        return entityPageToDTOPage(postPage);
    }
    @Transactional
    public boolean toggleLike(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1️⃣ 判断是否已点赞
        Optional<UserLikePost> existing = userLikePostRepository
                .findByPostIdAndUserId(postId, userId);

        if (existing.isPresent()) {
            // 已点赞 → 取消点赞
            userLikePostRepository.delete(existing.get());
            if (redisConnectionChecker.isRedisConnected()) {
                postStatsService.decreasePostLikeCount(postId);
                return  true;
            }
            LogUtil.redisFailLog();
            postRepository.decreaseLikeCountByPostId(postId);
            Post post = postRepository.findById(postId).orElseThrow(()->new RuntimeException("意外的错误"));

            double hotScore = CalculateUtil.calculatePostHotScore(post.getViewCount(),post.getLikeCount(),post.getCommentCount(),post.getFavouriteCount(), post.getCreateTime());
            postRepository.updateHotScore(postId, hotScore);
            return  true;

        } else {
            //  未点赞 → 点赞
            UserLikePost like = new UserLikePost();
            like.setPostId(postId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            userLikePostRepository.save(like);
            if(redisConnectionChecker.isRedisConnected()){
                postStatsService.increasePostLikeCount(postId);
            return false;
            }
            LogUtil.redisFailLog();
            postRepository.increaseLikeCountByPostId(postId);
            Post post = postRepository.findById(postId).orElseThrow(()->new RuntimeException("意外的错误"));

            double hotScore = CalculateUtil.calculatePostHotScore(post.getViewCount(),post.getLikeCount(),post.getCommentCount(),post.getFavouriteCount(),post.getCreateTime());
            postRepository.updateHotScore(postId, hotScore);
            return false;
        }
    }
    @Transactional
    public boolean toggleFavourite(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if(postRepository.findById(postId).isEmpty()) {
            throw new RuntimeException("找不到该文章");
        }
        //找不到->点赞
        if(userFavouritePostRepository.findByPostIdAndUserId(postId,userId).isEmpty()){
            UserFavouritePost userFavouritePost = new UserFavouritePost();
            userFavouritePost.setPostId(postId);
            userFavouritePost.setUserId(userId);
            userFavouritePost.setCreateDate(LocalDateTime.now());
            userFavouritePostRepository.save(userFavouritePost);
            if(redisConnectionChecker.isRedisConnected()){
                postStatsService.increasePostFavouriteCount(postId);
                return false;
            }
            LogUtil.redisFailLog();
            postRepository.increaseFavouriteCountByPostId(postId);
            Post post = postRepository.findById(postId).get();
            double hotScore = CalculateUtil.calculatePostHotScore(post.getViewCount(),post.getLikeCount(),post.getCommentCount(),post.getFavouriteCount(),post.getCreateTime());
            postRepository.updateHotScore(postId, hotScore);
            return false;
        }
        if (userFavouritePostRepository.findByPostIdAndUserId(postId,userId).isPresent()){
            userFavouritePostRepository.deleteByPostIdAndUserId(postId,userId);
            if(redisConnectionChecker.isRedisConnected()){
                postStatsService.decreasePostFavouriteCount(postId);
                return true;
            }
            LogUtil.redisFailLog();
            postRepository.decreaseFavouriteCountByPostId(postId);
            Post post = postRepository.findById(postId).get();
            double hotScore = CalculateUtil.calculatePostHotScore(post.getViewCount(),post.getLikeCount(),post.getCommentCount(),post.getFavouriteCount(),post.getCreateTime());
            postRepository.updateHotScore(postId, hotScore);
            return true;
        }
        throw new RuntimeException("意外的错误");
    }
    public Page<PostSimpleDTO> getUserOwnFavouritePosts(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = "";
        }
        Pageable pageable = PageRequest.of(page, size);
        Long userId = SecurityUtils.getCurrentUserId();


        Page<Post> pagePost = userFavouritePostRepository.searchUserFavouritePostByKeyword(userId, keyword, pageable);
        return entityPageToDTOPage(pagePost);
    }
}
