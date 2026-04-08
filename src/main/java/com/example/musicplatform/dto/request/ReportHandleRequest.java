package com.example.musicplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportHandleRequest {
    @NotNull
    private Long id;
    @NotNull
    private  String handlerNote;
    @NotNull
    private String result;

}
