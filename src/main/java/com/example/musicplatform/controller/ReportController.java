package com.example.musicplatform.controller;

import com.example.musicplatform.common.Response;
import com.example.musicplatform.dto.request.ReportCreateRequest;
import com.example.musicplatform.dto.request.ReportHandleRequest;
import com.example.musicplatform.entity.Report;
import com.example.musicplatform.service.ReportService;
import com.example.musicplatform.service.SseAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReportController {
    @Autowired
    ReportService reportService;
    @Autowired
    SseAdminService sseAdminService;

    @PostMapping("/report/create")
    public Response<?> createReport(@RequestBody ReportCreateRequest report){
        try {
            reportService.createReport(report);
            return Response.success(null,"举报成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+" 举报失败");
        }
    }
    @GetMapping("/admin/report/getNext")
    public Response<?> getNextReport(){
        try {
            Report report = sseAdminService.getNext();
            if(report==null){
                return Response.success(null,"无待处理举报");
            }
            return Response.success(report,"获取待处理举报成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+"获取待处理举报失败");
        }
    }
    @PostMapping("/admin/report/handle")
    public Response<?> handleReport(@RequestBody ReportHandleRequest reportHandleRequest){
        try {
            reportService.handleReport(reportHandleRequest);
            return Response.success(null,"处理举报成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+"  处理举报失败");
        }
    }
    @GetMapping("/report/getList")
    public Response<?> getReportList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try {
            return Response.success(reportService.getMyReportList(page, size),"获取举报列表成功");
        }catch (Exception e){
            return Response.error(null,e.getMessage()+"  获取举报列表失败");
        }
    }
}
