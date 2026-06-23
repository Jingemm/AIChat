package com.aichat.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentUploadDTO {
    private String title;        // 文档标题
    private MultipartFile file;  // 上传的文件
}