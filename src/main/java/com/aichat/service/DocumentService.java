package com.aichat.service;

import com.aichat.dto.DocumentUploadDTO;
import com.aichat.entity.Document;

public interface DocumentService {
    Document upload(DocumentUploadDTO dto) throws Exception;
}