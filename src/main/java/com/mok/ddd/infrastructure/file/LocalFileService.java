package com.mok.ddd.infrastructure.file;

import com.mok.ddd.application.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileService implements FileService {

    @Value("${file.storage.local.path:./uploads}")
    private String uploadPath;

    @Value("${file.storage.local.domain:http://localhost:8080}")
    private String domain;

    @Value("${file.storage.local.prefix:/files}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file) {
        try {
            return upload(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BizException("文件上传失败");
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
        try {
            // 生成日期目录
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String relativePath = dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + getExtension(fileName);
            
            // 确保目录存在
            Path targetDir = Paths.get(uploadPath, dateDir);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // 保存文件
            Path targetPath = Paths.get(uploadPath, relativePath);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL
            return domain + urlPrefix + "/" + relativePath;
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BizException("文件保存失败");
        }
    }

    @Override
    public void delete(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        
        try {
            // 从URL中解析出相对路径
            String prefix = domain + urlPrefix + "/";
            if (url.startsWith(prefix)) {
                String relativePath = url.substring(prefix.length());
                Path filePath = Paths.get(uploadPath, relativePath);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", url, e);
        }
    }

    private String getExtension(String fileName) {
        if (StringUtils.hasText(fileName) && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
