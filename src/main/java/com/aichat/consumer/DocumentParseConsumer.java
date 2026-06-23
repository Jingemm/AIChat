package com.aichat.consumer;

import com.aichat.entity.Document;
import com.aichat.mapper.DocumentMapper;
import com.aichat.service.EsDocumentService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "document-parse-topic", consumerGroup = "doc-parse-consumer")
public class DocumentParseConsumer implements RocketMQListener<Long> {
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private EsDocumentService esDocumentService;

    @Override
    public void onMessage(Long docId) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) return;
        try {
            System.out.println("正在异步索引文档到ES：" + doc.getTitle());
            esDocumentService.indexDocument(doc);
            doc.setStatus(2); // 完成
            documentMapper.updateById(doc);
            System.out.println("文档索引完成：" + doc.getTitle());
        } catch (Exception e) {
            e.printStackTrace();
            doc.setStatus(-1); // 失败
            documentMapper.updateById(doc);
        }
    }
}