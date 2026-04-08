package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.service.SseService;
import com.example.musicplatform.util.SecurityUtils;
import com.example.musicplatform.util.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {
    @Autowired
    private SseEmitterManager sseEmitterManager;
    @Autowired
    private SseService sseService;

    @GetMapping("/connect")
    public SseEmitter connect() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if(!sseService.isAdmin())
        {

            return sseEmitterManager.connect(currentUserId);
        }
        return sseEmitterManager.adminConnect(currentUserId);
    }
    @GetMapping("/sse/heartBeat")
    public Response<?> heartBeat() {
        return Response.success(null,"heartBeat");
    }
}
