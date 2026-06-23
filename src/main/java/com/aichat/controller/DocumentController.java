package com.aichat.controller;

import com.aichat.dto.DocumentUploadDTO;
import com.aichat.entity.Document;
import com.aichat.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public Document upload(DocumentUploadDTO dto) throws Exception {
        return documentService.upload(dto);
    }
}