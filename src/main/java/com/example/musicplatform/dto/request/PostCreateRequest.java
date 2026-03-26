package com.example.musicplatform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private String title;
    private String content;
    List<Long> mediaIdList;

}
