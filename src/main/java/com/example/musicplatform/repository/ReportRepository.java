package com.example.musicplatform.repository;

import com.example.musicplatform.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Report findFirstByStatusAndHandlerIdIsNullOrderByCreateTimeAsc(Report.ReportStatus status);
    List<Report> findAllByStatusAndHandlerId(Report.ReportStatus status, Long handlerId);
    Page<Report> findAllByReporterId(Long reporterId, Pageable pageable);
    Page<Report> findByStatusOrderByCreateTimeAsc(
            Report.ReportStatus status,
            Pageable pageable
    );
    @Modifying
    @Query("""
        UPDATE Report r
        SET r.status = 'HANDLING',
            r.handlerId = :handlerId
        WHERE r.id = :id
        AND r.status = 'PENDING'
        """)
    int claimReport(@Param("id") Long id,
                    @Param("handlerId") Long handlerId);
}
