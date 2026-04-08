package com.example.musicplatform.controller;


import com.example.musicplatform.common.Response;
import com.example.musicplatform.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class AdminController {
    @Autowired
    private AdminService adminService;
    @DeleteMapping("/admin/banUser/{id}")
    public Response<?> banUser(@PathVariable Long id){
        try {
            adminService.banUser(id);
            return Response.success(null,"封禁成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 封禁失败");
        }
    }
    @DeleteMapping("/admin/banPost/{id}")
    public Response<?> banPost(@PathVariable Long id){
        try {
            adminService.banPost(id);
            return Response.success(null,"封禁成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 封禁失败");
        }
    }
    @DeleteMapping("/admin/banPostComment/{id}")
    public Response<?> banPostComment(@PathVariable Long id){
        try {
            adminService.banPostComment(id);
            return Response.success(null,"封禁成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 封禁失败");
        }
    }
    @DeleteMapping("/admin/banSong/{id}")
    public Response<?> banSong(@PathVariable Long id){
        try {
            adminService.banSong(id);
            return Response.success(null,"封禁成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 封禁失败");
        }
    }
    @DeleteMapping("/admin/banSongComment/{id}")
    public Response<?> banSongComment(@PathVariable Long id){
        try {
            adminService.banSongComment(id);
            return Response.success(null,"封禁成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 封禁失败");
        }
    }
    @PostMapping("/superAdmin/addAdmin/{id}")
    public Response<?> addAdmin(@PathVariable  Long id){
        try {
            adminService.addAdmin(id);
            return Response.success(null," 添加管理员成功");
        }catch (Exception e){
            return Response.error(null,"添加管理员失败");
        }
    }

}

