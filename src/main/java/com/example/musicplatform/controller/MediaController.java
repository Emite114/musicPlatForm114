package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.entity.Media;
import com.example.musicplatform.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;



@RestController
public class MediaController {
    @Autowired
    private MediaService mediaService;
    @PostMapping("/user/uploadMedia")//上传到本地候返回本地文件地址
    public Response<?> uploadImage(@RequestParam("file") MultipartFile file){
        if (file.isEmpty()){
            return Response.error(null,"文件不能为空");
        }
        try {
           Media media =mediaService.uploadFile(file);
            return Response.success(Map.of(
                    "mediaId", media.getId(),
                    "url", "/media/" + media.getUrl(),
                    "originalName",media.getOriginalName()
                    ),"上传成功");
        } catch (Exception e) {
            return Response.error(null,e.getMessage());
        }
    }


}
