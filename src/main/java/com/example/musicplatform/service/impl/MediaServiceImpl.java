package com.example.musicplatform.service.impl;

import com.example.musicplatform.entity.Media;
import com.example.musicplatform.repository.MediaRepository;
import com.example.musicplatform.service.MediaService;
import com.example.musicplatform.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private MediaRepository mediaRepository;




    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        String ext = filename.substring(filename.lastIndexOf('.'));
        ext = ext.toLowerCase();
        if(ext.equalsIgnoreCase(".mp3")|| ext.equals(".wav")) {
            return ext;}
        if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") ) {
            return  ext;}
        if(ext.equals(".lrc")){
            return ext;
        }
        throw new RuntimeException("不支持的文件扩展名");
    }
    @Override
    public Media uploadFile(MultipartFile file) throws IOException {
        Media.MediaType mediaType =null;
        if(file.getContentType()==null)throw new RuntimeException("文件类型为空");
        //100mb
        if (file.getSize() > 100 * 1024 * 1024) throw new RuntimeException("文件超过100MB!");

        //todo:换服务器,oss,MinIO
        String baseDir = "D:/AAAmusicplatformMedia";

        // 2. 原始文件名
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) throw new RuntimeException("空的文件名");
        String extension = getFileExtension(originalFileName);

        if (extension.equals(".mp3") || extension.equals(".wav")) {
            mediaType= Media.MediaType.audio;
        }else if (extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png")) {
            mediaType= Media.MediaType.image;
        } else if (extension.equals(".lrc")) {
            mediaType= Media.MediaType.lrc;

        } else throw new RuntimeException("mediaType初始化失败");

        // 3. 生成 UUID
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 4. 分片目录
        String subdir1 = uuid.substring(0, 2);
        String subdir2 = uuid.substring(2, 4);

        Path targetDir = Paths.get(baseDir, subdir1, subdir2);
        Files.createDirectories(targetDir);

        // 5. 最终文件路径
        String finalFileName = uuid + extension;
        Path finalPath = targetDir.resolve(finalFileName);

        // 6. 保存文件（流式）
        Files.copy(file.getInputStream(), finalPath, StandardCopyOption.REPLACE_EXISTING);

        // 7. 存数据库
        Media media = new Media();
        media.setMediaType(mediaType);
        media.setSize(file.getSize());
        media.setUserId(SecurityUtils.getCurrentUserId());
        media.setOriginalName(originalFileName);
        media.setUrl(subdir1 + "/" + subdir2 + "/" + finalFileName);
        media.setCreateDate(LocalDateTime.now());
        // 8. 返回访问路径（推荐返回相对路径）
        return mediaRepository.save(media);
//                "/media/" + media.getUrl();
    }

}
