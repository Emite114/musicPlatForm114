package com.example.musicplatform.service.impl;

import com.example.musicplatform.dto.request.ReportCreateRequest;
import com.example.musicplatform.dto.request.ReportHandleRequest;
import com.example.musicplatform.dto.response.ReportSimpleResponse;
import com.example.musicplatform.entity.Report;
import com.example.musicplatform.repository.*;
import com.example.musicplatform.service.ReportService;
import com.example.musicplatform.util.SecurityUtils;
import com.example.musicplatform.util.SseEmitterManager;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SongRepository songRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostCommentRepository postCommentRepository;
    @Autowired
    SongCommentRepository songCommentRepository;
    @Override
    public void releaseReports(Long adminId) {
        List<Report> unHandledReports = reportRepository.findAllByStatusAndHandlerId(Report.ReportStatus.HANDLING,adminId);
        for (Report report : unHandledReports) {
            report.setStatus(Report.ReportStatus.PENDING);
            report.setHandlerId(null);
            reportRepository.save(report);
        }
    }

    @Override
    public void createReport(ReportCreateRequest request) {
        if (request == null) {throw new IllegalArgumentException("请求不能为空");}
        if(request.getTargetType() == Report.TargetType.POST){
            if(postRepository.findById(request.getTargetId()).isEmpty()){
                throw new RuntimeException("找不到目标帖子");
            }
        }
        if(request.getTargetType() == Report.TargetType.SONG){
            if(songRepository.findById(request.getTargetId()).isEmpty()){
                throw new RuntimeException("找不到目标歌曲");
            }
        }
        if(request.getTargetType() == Report.TargetType.POST_COMMENT){
            if(postCommentRepository.findById(request.getTargetId()).isEmpty()){
                throw new RuntimeException("找不到目标帖子评论");
            }
        }
        if(request.getTargetType() == Report.TargetType.SONG_COMMENT){
            if(songCommentRepository.findById(request.getTargetId()).isEmpty()){
                throw new RuntimeException("找不到目标歌曲评论");
            }
        }if(request.getTargetType() == Report.TargetType.USER){
            if(userRepository.findById(request.getTargetId()).isEmpty()){
                throw new RuntimeException("找不到目标用户");
            }
        }
        Report report = new Report();
        report.setReporterId(SecurityUtils.getCurrentUserId());
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason());
        report.setCreateTime(LocalDateTime.now());
        report.setStatus(Report.ReportStatus.PENDING);
        reportRepository.save(report);
    }


    @Override
    public void handleReport(ReportHandleRequest request) {
        if (request == null) {throw new IllegalArgumentException("请求不能为空");}
        Report report = reportRepository.findById(request.getId()).orElseThrow(()->new RuntimeException("找不到该举报"));
        if(!Objects.equals(report.getHandlerId(), SecurityUtils.getCurrentUserId())){
            throw new RuntimeException("只准处理自己领取的举报");
        }
        report.setHandledTime(LocalDateTime.now());
        report.setHandlerNote(request.getHandlerNote());
        report.setResult(request.getResult());
        if (Objects.equals(report.getResult(), "REJECT")){
            report.setStatus(Report.ReportStatus.REJECTED);
        }
        if (Objects.equals(report.getResult(), "PASS")){
            report.setStatus(Report.ReportStatus.RESOLVED);
        }
        reportRepository.save(report);
    }

    @Override
    public Page<ReportSimpleResponse> getMyReportList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportPage = reportRepository.findAllByReporterId(SecurityUtils.getCurrentUserId(),pageable);
        return reportPage.map(report -> {
            ReportSimpleResponse rsr = new ReportSimpleResponse();
            rsr.setReason(report.getReason());
            rsr.setResult(report.getResult());
            rsr.setCreateTime(report.getCreateTime());
            rsr.setHandlerNote(report.getHandlerNote());
            rsr.setTargetId(report.getTargetId());
            rsr.setTargetType(report.getTargetType());
            return rsr;
        });
    }
}
