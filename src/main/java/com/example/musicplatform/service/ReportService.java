package com.example.musicplatform.service;

import com.example.musicplatform.dto.request.ReportCreateRequest;
import com.example.musicplatform.dto.request.ReportHandleRequest;
import com.example.musicplatform.dto.response.ReportSimpleResponse;
import com.example.musicplatform.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    void releaseReports(Long adminId);
    void createReport(ReportCreateRequest reportCreateRequest);
    void handleReport(ReportHandleRequest reportHandleRequest);
    Page<ReportSimpleResponse> getMyReportList(int page, int size);
}
