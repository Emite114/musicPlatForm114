package com.example.musicplatform.service.impl;

import com.example.musicplatform.converters.ConversationConverter;
import com.example.musicplatform.converters.MessageConverter;
import com.example.musicplatform.dto.event.MessageEvent;
import com.example.musicplatform.dto.request.SendMessageRequest;
import com.example.musicplatform.dto.response.ConversationListResponse;
import com.example.musicplatform.dto.response.CreateMessageResponse;
import com.example.musicplatform.dto.response.MessageResponse;
import com.example.musicplatform.entity.Conversation;
import com.example.musicplatform.entity.Message;
import com.example.musicplatform.entity.User;
import com.example.musicplatform.repository.ConversationRepository;
import com.example.musicplatform.repository.FollowRepository;
import com.example.musicplatform.repository.MessageRepository;
import com.example.musicplatform.repository.UserRepository;
import com.example.musicplatform.service.MessageService;
import com.example.musicplatform.util.SecurityUtils;
import com.example.musicplatform.util.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private SseEmitterManager sseEmitterManager;
    @Autowired
    private ConversationRepository  conversationRepository;
    @Autowired
    private ConversationConverter conversationConverter;

    @Transactional
    Conversation getOrCreateConversation(Long speakingUserId,Long receiveUserId) {
        Long u1 = Math.max(speakingUserId,receiveUserId);
        Long u2 = Math.min(speakingUserId,receiveUserId);
        if(conversationRepository.findByUser1IdAndUser2Id(u1,u2).isPresent()){
            return conversationRepository.findByUser1IdAndUser2Id(u1,u2).get();
        }
        if (conversationRepository.findByUser1IdAndUser2Id(u1,u2).isEmpty()) {
            Conversation conversation = new Conversation();
            conversation.setUser1Id(u1);
            conversation.setUser2Id(u2);
            conversation.setCreateTime(LocalDateTime.now());
            return conversationRepository.save(conversation);
        }
        else {
            throw new RuntimeException("意外的错误");
        }
    }

    @Transactional
    CreateMessageResponse CreateMessage(SendMessageRequest request){
        if(request.getContent() ==null){
            throw new IllegalArgumentException("不能发送空消息");
        }
        LocalDateTime now = LocalDateTime.now();
        userRepository.findById(request.getReceiveUserId()).orElseThrow(()-> new IllegalArgumentException("找不到目标用户"));

        Message message = messageConverter.requestToEntity(request);
        message.setSpeakingUserId(SecurityUtils.getCurrentUserId());
        message.setCreateTime(now);

        Conversation conversation = getOrCreateConversation(message.getSpeakingUserId(), message.getReceiveUserId());
        message.setConversationId(conversation.getId());
        messageRepository.save(message);
        Long unreadCount =0L;
        if(Objects.equals(conversation.getUser1Id(), message.getReceiveUserId())){
            conversation.setUnreadCount1(conversation.getUnreadCount1()+1);
            unreadCount=conversation.getUnreadCount1();
        }
        if(Objects.equals(conversation.getUser2Id(), message.getReceiveUserId())){
            conversation.setUnreadCount2(conversation.getUnreadCount2()+1);
            unreadCount=conversation.getUnreadCount2();
        }
        conversation.setUpdateTime(now);
        conversation.setLastMessageSenderId(message.getSpeakingUserId());
        conversation.setLastMessageContent(message.getContent());
        conversation.setLastTime(now);
        conversationRepository.save(conversation);

        CreateMessageResponse response = messageConverter.entityToCreateMessageResponse(message);
        response.setLastMessage(conversation.getLastMessageContent());
        response.setLastMessageSenderId(conversation.getLastMessageSenderId());
        response.setLastMessageTime(now);
        response.setUnreadCount(unreadCount);
        //Sse推送
        MessageEvent messageEvent = MessageEvent.builder()
                .type("NEW_MESSAGE")
                .conversationId(message.getConversationId())
                .speakingUserId(message.getSpeakingUserId())
                .receiveUserId(message.getReceiveUserId())
                .content(message.getContent())
                .createTime(now)
                .unreadCount(unreadCount)
                .build();

        sseEmitterManager.sendMessageToReceiver(message.getReceiveUserId(),messageEvent);
        return response;
    }

    @Transactional
    @Override
    public CreateMessageResponse sendMessage(SendMessageRequest request) {
        Long speakingUserId = SecurityUtils.getCurrentUserId();
        Long receiveUserId = request.getReceiveUserId();
        if(speakingUserId.equals(receiveUserId))throw new IllegalArgumentException("不能私信自己");

        //对方关注了你,就可以无限制发
        if(followRepository.findByUserIdAndFollowUserId(receiveUserId,speakingUserId).isPresent()){
            return CreateMessage(request);
        }
        //未任何关系且 发消息的人还 未发任何消息时 可以发一条
        if(!messageRepository.existsBySpeakingUserIdAndReceiveUserId(receiveUserId,speakingUserId)){
            return CreateMessage(request);
        }
        //互相发了一条消息后即可继续
        if(messageRepository.existsBySpeakingUserIdAndReceiveUserId(speakingUserId,receiveUserId)
                &&messageRepository.existsBySpeakingUserIdAndReceiveUserId(receiveUserId,speakingUserId)){
            return CreateMessage(request);
        }
        else{
            throw new IllegalStateException("在对方关注你或发第一条消息之前,只允许发一条消息");
        }
    }

    @Override
    public Page<ConversationListResponse> getConversationList(int page,int size) {
        Pageable pageable = PageRequest.of(page,size);
        return conversationRepository.pageByUserId(SecurityUtils.getCurrentUserId(),pageable).map(conversation -> {
            ConversationListResponse clr = conversationConverter.entityToResponse(conversation);
            User talkingTouser =new User();
            User currentUser =new User();
            if(conversation.getUser1Id().equals(SecurityUtils.getCurrentUserId())){
                talkingTouser = userRepository.findById(conversation.getUser2Id()).orElseThrow(()->new RuntimeException("意外的错误,找不到对方用户"));
                currentUser = userRepository.findById(conversation.getUser1Id()).orElseThrow(()->new RuntimeException("意外的错误,找不到当前用户"));
                clr.setUnreadCount(conversation.getUnreadCount1());
            }
            if(conversation.getUser2Id().equals(SecurityUtils.getCurrentUserId())){
                talkingTouser = userRepository.findById(conversation.getUser1Id()).orElseThrow(()->new RuntimeException("意外的错误,找不到对方用户"));
                currentUser=userRepository.findById(conversation.getUser2Id()).orElseThrow(()->new RuntimeException("意外的错误,找不到当前用户"));
                clr.setUnreadCount(conversation.getUnreadCount2());
            }
            clr.setTalkingToUserId(talkingTouser.getId());
            clr.setTalkingToUserName(talkingTouser.getUsername());

            if (talkingTouser.getAvatarUrl() != null) {
                clr.setTalkingToUserAvatar(talkingTouser.getAvatarUrl());
            }
            if(currentUser.getAvatarUrl()!=null){
                clr.setUserAvatar(currentUser.getAvatarUrl());
            }
            clr.setLastMessageSenderName(userRepository.findById(clr.getLastMessageSenderId()).orElseThrow(()->new RuntimeException("意外的错误")).getUsername());
            return clr;
        });
    }

    @Transactional
    @Override
    public Page<MessageResponse> getMessages(Long conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        return messageRepository.findByConversationIdOrderByCreateTime(conversationId,pageable).map(message -> {
            MessageResponse mr = messageConverter.entityToMessageResponse(message);
            Long currentUserId = SecurityUtils.getCurrentUserId();
            Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(()->new RuntimeException("意外的错误,找不到对话"));
            if(conversation.getUser1Id().equals(SecurityUtils.getCurrentUserId())){
                conversation.setUnreadCount1(0L);
            }
            if(conversation.getUser2Id().equals(SecurityUtils.getCurrentUserId())){
                conversation.setUnreadCount2(0L);
            }
            conversationRepository.save(conversation);
            return mr;
        });
    }

    @Transactional
    @Override
    public void markAsReadBecauseTheUserIsReadingTheConversation(Long conversationId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Conversation conversation =conversationRepository.findById(conversationId).orElseThrow(()->new RuntimeException("意外的错误,找不到对话"));
        if(conversation.getUser1Id().equals(currentUserId)){
            conversation.setUnreadCount1(0L);
        }
        else if(conversation.getUser2Id().equals(currentUserId)){
            conversation.setUnreadCount2(0L);
        }
        else throw new RuntimeException("意外的错误,该用户不属于该对话");
        conversationRepository.save(conversation);
    }
}

