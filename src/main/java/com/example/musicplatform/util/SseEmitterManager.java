package com.example.musicplatform.util;

import com.example.musicplatform.dto.event.MessageEvent;
import com.example.musicplatform.service.ReportService;
import com.example.musicplatform.service.SseService;
import com.example.musicplatform.service.UserService;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SseEmitterManager {
    @Autowired
    ReportService reportService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> adminEmitters = new ConcurrentHashMap<>();
    @Autowired
    private SseService sseService;
    @Autowired
    private UserService userService;

    public SseEmitter connect(Long userId){
        SseEmitter sseEmitter = new SseEmitter(10*60*1000L);
        userEmitters.put(userId, sseEmitter);

        sseEmitter.onCompletion(() -> userEmitters.remove(userId));
        sseEmitter.onTimeout(() -> userEmitters.remove(userId));
        sseEmitter.onError((e) -> userEmitters.remove(userId));

        // 延迟 3 秒发送
        scheduler.schedule(() -> {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("connected")
                        .data("连接成功，当前时间: " + LocalDateTime.now()));
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        }, 3, TimeUnit.SECONDS);

        return sseEmitter;
    }
    public SseEmitter adminConnect(Long userId){
        SseEmitter sseEmitter = new SseEmitter(10*60*1000L);
        adminEmitters.put(userId, sseEmitter);

        sseEmitter.onCompletion(() -> {
            adminEmitters.remove(userId);
            reportService.releaseReports(userId);
        });
        sseEmitter.onTimeout(() -> {
            adminEmitters.remove(userId);
            reportService.releaseReports(userId);
        });
        sseEmitter.onError((e) -> {
            adminEmitters.remove(userId);
            reportService.releaseReports(userId);
        });
        scheduler.schedule(() -> {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("connected")
                        .data("连接成功，当前时间: " + LocalDateTime.now()));
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
        }, 3, TimeUnit.SECONDS);
        return sseEmitter;
    }

    public void sendMessageToReceiver(Long receiveUserId, MessageEvent messageEvent){
        SseEmitter sseEmitter = new SseEmitter();
        if(userService.isAdmin(receiveUserId)){
            sseEmitter = adminEmitters.get(receiveUserId);
        }else{
        sseEmitter = userEmitters.get(receiveUserId);}
        if(sseEmitter != null){
            try{
                sseEmitter.send(messageEvent);
            }catch(Exception e){
                userEmitters.remove(receiveUserId);
            }
        }
    }
    public boolean isAdminOnline(Long adminUserId) {
        return adminEmitters.containsKey(adminUserId);
    }
}
