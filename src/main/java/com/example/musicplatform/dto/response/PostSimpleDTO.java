package com.example.musicplatform.dto.response;

import lombok.Data;

@Data
public class PostSimpleDTO {
    private Long id;
    private String title;
    private String content;
    private String coverUrl;
}
