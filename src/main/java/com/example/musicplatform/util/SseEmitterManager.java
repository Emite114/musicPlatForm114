package com.example.musicplatform.util;

import com.example.musicplatform.dto.event.MessageEvent;
import com.example.musicplatform.service.ReportService;
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

    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private final Map<Long, SseEmitter> adminEmitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long userId){
        SseEmitter sseEmitter = new SseEmitter(10*60*1000L);
        userEmitters.put(userId, sseEmitter);

        sseEmitter.onCompletion(() -> userEmitters.remove(userId));
        sseEmitter.onTimeout(() -> userEmitters.remove(userId));
        sseEmitter.onError((e) -> userEmitters.remove(userId));

        try {
            sseEmitter.send(SseEmitter.event().name("connected").data("连接成功,当前时间:"+ LocalDateTime.now()));
        }catch (Exception e){
            sseEmitter.completeWithError(e);
        }
        //todo心跳应该由前端发送

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
        return sseEmitter;
    }

    public void sendMessageToReceiver(Long receiveUserId, MessageEvent messageEvent){
        SseEmitter emitter = userEmitters.get(receiveUserId);
        if(emitter != null){
            try{
                emitter.send(messageEvent);
            }catch(Exception e){
                userEmitters.remove(receiveUserId);
            }
        }
    }
    public boolean isAdminOnline(Long adminUserId) {
        return adminEmitters.containsKey(adminUserId);
    }
}
