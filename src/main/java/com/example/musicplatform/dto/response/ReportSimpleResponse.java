package com.example.musicplatform.dto.response;

import com.example.musicplatform.entity.Report;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportSimpleResponse {
    Report.TargetType targetType;
    Long targetId;
    String reason;
    LocalDateTime createTime;
    String result;
    String handlerNote;

}
