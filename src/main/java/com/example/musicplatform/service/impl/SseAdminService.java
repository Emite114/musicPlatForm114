package com.example.musicplatform.service.impl;

import com.example.musicplatform.entity.Report;
import com.example.musicplatform.repository.ReportRepository;
import com.example.musicplatform.util.SecurityUtils;
import com.example.musicplatform.util.SseEmitterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SseAdminService implements com.example.musicplatform.service.SseAdminService {
    @Autowired
    SseEmitterManager sseEmitterManager;
    @Autowired
    ReportRepository reportRepository;

//    public Report getNextReport() {
//        Long adminId = SecurityUtils.getCurrentUserId();
//        if(!sseEmitterManager.isAdminOnline(adminId)){
//            throw new RuntimeException("当前管理员不在线,未通过sse连接 ");
//        }
//        Report report = reportRepository.findFirstByStatusAndHandlerIdIsNullOrderByCreateTimeAsc(Report.ReportStatus.PENDING);
//        if (report == null) {
//            return null;
//        }
//        report.setHandlerId(adminId);
//        report.setStatus(Report.ReportStatus.HANDLING);
//        reportRepository.save(report);
//        return report;
//    }
    @Transactional
    public Report getNext(){
        Long adminId = SecurityUtils.getCurrentUserId();
        if(!sseEmitterManager.isAdminOnline(adminId)){
            throw new RuntimeException("当前管理员不在线,未通过sse连接 ");
        }
        Pageable pageable = PageRequest.of(0, 1);
        Page<Report> reportPage = reportRepository.findByStatusOrderByCreateTimeAsc(Report.ReportStatus.PENDING,pageable);
        if(reportPage.getTotalElements() == 0){
            throw new RuntimeException("全部举报已处理完毕");
        }
        Report report = reportPage.getContent().get(0);
        int update  = reportRepository.claimReport(report.getId(),adminId);
        if(update == 1){
            report.setStatus(Report.ReportStatus.HANDLING);
            report.setHandlerId(adminId);
            return report;
        }
        else {
            return getNext();
        }
    }
}
