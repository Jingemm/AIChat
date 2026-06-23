package com.aichat.service.impl;

import com.aichat.dto.DocumentUploadDTO;
import com.aichat.entity.Document;
import com.aichat.mapper.DocumentMapper;
import com.aichat.service.DocumentService;
import com.aichat.service.EsDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DocumentServiceImpl implements DocumentService {
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private EsDocumentService esDocumentService;

    // 创建一个线程池（固定 5 个线程，够用了）
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    public Document upload(DocumentUploadDTO dto) throws Exception {
        MultipartFile file = dto.getFile();
        String content = new String(file.getBytes(), "UTF-8");
        String originalFilename = file.getOriginalFilename();
        String fileType = originalFilename.substring(originalFilename.lastIndexOf("."));

        Document doc = new Document();
        doc.setTitle(dto.getTitle());
        doc.setContent(content);
        doc.setFileType(fileType);
        doc.setStatus(1); // 1 = 解析中
        documentMapper.insert(doc);

        // 用线程池异步处理 ES 索引（替代 RocketMQ）
        executor.submit(() -> {
            try {
                System.out.println("正在异步索引文档到ES：" + doc.getTitle());
                esDocumentService.indexDocument(doc);
                doc.setStatus(2); // 2 = 完成
                documentMapper.updateById(doc);
                System.out.println("文档索引完成：" + doc.getTitle());
            } catch (Exception e) {
                e.printStackTrace();
                doc.setStatus(-1); // -1 = 失败
                documentMapper.updateById(doc);
            }
        });

        // 立即返回，不等待异步完成
        return doc;
    }
}