package com.example.musicplatform.dto.request;

import com.example.musicplatform.entity.Report;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportCreateRequest {
    @NotNull
    Report.TargetType targetType;
    @NotNull
    Long targetId;
    @NotNull
    String reason;

}
