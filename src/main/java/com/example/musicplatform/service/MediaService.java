package com.example.musicplatform.service;

import com.example.musicplatform.entity.Media;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface MediaService {
    Media uploadFile(MultipartFile file) throws IOException;

}
