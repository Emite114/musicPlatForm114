package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.dto.request.SendMessageRequest;
import com.example.musicplatform.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/message/sendMessage")
    public Response<?> sendMessage(@RequestBody SendMessageRequest request){
        try {
            return Response.success(messageService.sendMessage(request),"消息发送成功");
        }
        catch (Exception e){
            return Response.error(null,e.getMessage()+" 消息发送失败");
        }
    }
    @GetMapping("/message/getConversationList")
    public Response<?> getConversationList(
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "20")int size
    ){
        try {
            return Response.success(messageService.getConversationList(page, size),"对话列表返回成功");
        }catch (Exception e){
            return Response.error(null, e.getMessage()+" 对话列表返回失败");
        }
    }
    @GetMapping("/message/getMessage")
    public Response<?> getMessage(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        try {
            return Response.success(messageService.getMessages(conversationId,page,size),"查找消息成功");
        }
        catch (Exception e){
            return Response.error(null, e.getMessage()+" 查找消息失败");
        }
    }
    @PostMapping("/message/markAsRead")
    public Response<?> markAsRead(@RequestBody Long conversationId){
        try {
            messageService.markAsReadBecauseTheUserIsReadingTheConversation(conversationId);
            return Response.success(null,"标记已读成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 标记已读失败");
        }
    }
}
