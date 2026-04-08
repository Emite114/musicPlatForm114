package com.example.musicplatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@Entity
@Table(name = "tb_report"
)
@AllArgsConstructor
@NoArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 举报人
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    // 被举报的类型（USER, POST,SONG,SONG_COMMENT,POST_COMMENT 等）
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    // 被举报的对象ID
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    // 举报原因
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // 举报状态：PENDING-待处理, HANDLING-处理中, RESOLVED-已处理, REJECTED-已驳回
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    // 当前处理人（管理员）
    @Column(name = "handler_id")
    private Long handlerId;

    // 处理时间
    @Column(name = "handled_time")
    private LocalDateTime handledTime;

    // 处理备注
    @Column(name = "handler_note", columnDefinition = "TEXT")
    private String handlerNote;

    // 处理结果：通过/PASS / 驳回/REJECT
    @Column(name = "result")
    private String result;

    // 创建时间
    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 版本号（用于乐观锁）
    @Version
    @Column(name = "version")
    private Long version = 0L;

    public enum ReportStatus {
        PENDING,    // 待处理
        HANDLING,   // 处理中（已被认领）
        RESOLVED,   // 已处理
        REJECTED    // 已驳回
    }
    public enum TargetType{
        USER,
        SONG,
        POST,
        SONG_COMMENT,
        POST_COMMENT
    }
}


