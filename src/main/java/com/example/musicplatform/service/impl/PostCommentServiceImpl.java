package com.example.musicplatform.service.impl;

import com.example.musicplatform.converters.PostCommentConverter;
import com.example.musicplatform.dto.request.PostCommentCreateRequest;
import com.example.musicplatform.dto.response.PostCommentResponse;
import com.example.musicplatform.entity.PostComment;
import com.example.musicplatform.entity.User;
import com.example.musicplatform.entity.UserLikePostComment;
import com.example.musicplatform.repository.PostCommentRepository;
import com.example.musicplatform.repository.PostRepository;
import com.example.musicplatform.repository.UserLikePostCommentRepository;
import com.example.musicplatform.repository.UserRepository;
import com.example.musicplatform.service.PostCommentService;
import com.example.musicplatform.service.UserService;
import com.example.musicplatform.service.redisService.PostStatsService;
import com.example.musicplatform.service.redisService.RedisConnectionChecker;
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
import java.util.Optional;

@Service
public class PostCommentServiceImpl implements PostCommentService {
    @Autowired
    PostCommentConverter postCommentConverter;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostCommentRepository postCommentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserLikePostCommentRepository userLikePostCommentRepository;
    @Autowired
    RedisConnectionChecker redisConnectionChecker;
    @Autowired
    PostStatsService postStatsService;



    @Transactional
    @Override
    public void createComment(PostCommentCreateRequest request) {
        if (request == null) {throw new RuntimeException("请求不能为空");}
        if (request.getPostId() == null) {throw new RuntimeException("帖子id不能为空");}
        if(request.getContent()==null){throw new RuntimeException("评论内容不能为空");}
        if(request.getParentId()==null||request.getParentId()==0L){
            if(postRepository.findById(request.getPostId()).isEmpty())
            {
                throw new RuntimeException("找不到该帖子");
            }
            PostComment postCommentParent = postCommentConverter.toEntity(request);
            postCommentParent.setParentId(0L);
            postCommentParent.setUserId(SecurityUtils.getCurrentUserId());
            postCommentParent.setReplyToUserId(0L);
            postCommentParent.setCreateTime(LocalDateTime.now());
            postCommentRepository.save(postCommentParent);
            postRepository.increaseCommentCountByPostId(postCommentParent.getPostId());
        }else {
            Optional<PostComment> op = postCommentRepository.findById(request.getParentId());
            op.orElseThrow(()->new RuntimeException("找不到根评论"));
            if (userRepository.findById(request.getReplyToUserId()).isEmpty()){
                throw new RuntimeException("找不到回复的用户");
            }
            PostComment postCommentChild = postCommentConverter.toEntity(request);
            postCommentChild.setParentId(request.getParentId());
            postCommentChild.setReplyToUserId(request.getReplyToUserId());
            postCommentChild.setCreateTime(LocalDateTime.now());
            postCommentChild.setUserId(SecurityUtils.getCurrentUserId());
            postCommentRepository.save(postCommentChild);
            if (redisConnectionChecker.isRedisConnected()){
                postStatsService.increasePostCommentCount(request.getPostId());
            }else {
                LogUtil.redisFailLog();
                postRepository.increaseCommentCountByPostId(postCommentChild.getPostId());
            }
        }


    }

    @Override
    //传入评论id
    @Transactional
    public boolean toggleLike(Long postCommentId) {
        if(postCommentRepository.findById(postCommentId).isPresent()){
            Long userId =SecurityUtils.getCurrentUserId();
            if(userLikePostCommentRepository.findByPostCommentIdAndUserId(postCommentId, userId).isPresent()){
                userLikePostCommentRepository.deleteByPostCommentIdAndUserId(postCommentId, userId);
                postCommentRepository.decreaseLikeCount(postCommentId);
                return true;
            }else {
                UserLikePostComment userLikePostComment = new UserLikePostComment();
                userLikePostComment.setPostCommentId(postCommentId);
                userLikePostComment.setUserId(userId);
                userLikePostComment.setCreateTime(LocalDateTime.now());
                userLikePostCommentRepository.save(userLikePostComment);
                postCommentRepository.increaseLikeCount(postCommentId);
                return false;
            }
        }else {throw new RuntimeException("该评论不存在");}
    }

    @Override
    public Page<PostCommentResponse> pagePostParentComments(Long postId, int page, int size,String sort) {
        if (postRepository.findById(postId).isEmpty()){
            throw new RuntimeException("找不到帖子");
        }
        Pageable pageable = PageableUtil.initializePageable(page, size, sort);


        Page<PostComment> postComments = postCommentRepository.findByPostIdAndParentIdAndIsDelete(postId,0L,pageable,false);
        return postComments.map(postComment -> {
            PostCommentResponse cmp = postCommentConverter.toDTO(postComment);
            cmp.setCountOfChildren(postCommentRepository.countByParentIdAndIsDelete(cmp.getId(),false));
            userRepository.findById(postComment.getUserId()).ifPresent(user -> cmp.setUserAvatar(user.getAvatarUrl()));
            return cmp;
        });
    }

    @Override
    public Page<PostCommentResponse> pagePostChildrenComments(Long parentId, int page, int size,String sort) {
        if(postCommentRepository.findById(parentId).isEmpty()){
            throw new RuntimeException("找不到父评论");
        }
        Pageable pageable = PageableUtil.initializePageable(page, size, sort);
        Page<PostComment> commentChildren = postCommentRepository.findByParentIdAndIsDelete(parentId,pageable,false);
        return commentChildren.map(postComment -> {
            PostCommentResponse cmp = postCommentConverter.toDTO(postComment);
            userRepository.findById(postComment.getUserId()).ifPresent(user -> cmp.setReplyToUserName(user.getUsername()));
            userRepository.findById(postComment.getUserId()).ifPresent(user -> cmp.setUserAvatar(user.getAvatarUrl()));
            return cmp;
        });
    }

    @Override
    public void deleteComment(Long commentId) {
        PostComment postComment = postCommentRepository.findById(commentId).orElse(null);
        if (postComment != null) {
            if(postComment.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            postComment.setIsDelete(true);
            }else {throw new RuntimeException("用户id不匹配");}
        }else {throw new RuntimeException("找不到该评论");}
        postCommentRepository.save(postComment);
        postRepository.findById(postComment.getPostId()).ifPresent(post -> {
            postRepository.decreaseCommentCountByPostId(post.getId());
        });
    }
}
