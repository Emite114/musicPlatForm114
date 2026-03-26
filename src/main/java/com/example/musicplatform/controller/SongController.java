package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.dto.request.UploadSongRequest;
import com.example.musicplatform.dto.response.SongDetailsResponse;
import com.example.musicplatform.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SongController {
    @Autowired
    private SongService songService;
    @PostMapping("/user/uploadSong")
    public Response<?> uploadSong(@RequestBody UploadSongRequest songRequest) {
        try {
            songService.uploadSong(songRequest);
            return Response.success(null,"歌曲上传成功");
        } catch (Exception e) {
            return Response.error(null,e.getMessage()+" 歌曲创建失败");
        }
    }
    @GetMapping("/song/getSong/")
    public Response<?> getSongDetails(@RequestParam("id") Long id){
        try {
            return Response.success(songService.getSongDetails(id),"歌曲成功返回");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+"歌曲初始化失败");
        }
    }
    @GetMapping("/song/page")
    public Response<?> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size
            ){
        try {
            return Response.success(songService.page(keyword,page,size),"查找成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+"查找歌曲页失败");
        }
    }
}
