package com.aichat.service.impl;

import com.aichat.entity.ChatHistory;
import com.aichat.mapper.ChatHistoryMapper;
import com.aichat.service.ChatService;
import com.aichat.service.EsDocumentService;
import com.aichat.util.LlmUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private EsDocumentService esDocumentService;
    @Autowired
    private LlmUtil llmUtil;
    @Autowired
    private ChatHistoryMapper chatHistoryMapper;

    @Override
    public String chat(String sessionId, String question) {
        // 1. 搜索相关知识
        List<Map<String, Object>> docs = new ArrayList<>();
        try {
            docs = esDocumentService.hybridSearch(question, 5);
        } catch (Exception ignored) {}
        String knowledge = docs.stream()
                .map(d -> (String) d.getOrDefault("content", ""))
                .collect(Collectors.joining("\n---\n"));

        // 2. 获取历史对话（最近10条）
        List<ChatHistory> histories = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getSessionId, sessionId)
                        .orderByAsc(ChatHistory::getCreateTime)
                        .last("limit 10")
        );

        // 3. 构建消息列表（System + History + User）
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "你是一个客服助手，请严格根据以下知识库内容回答用户问题。如果知识库中没有，就说不知道。\n知识库：\n" + knowledge));
        for (ChatHistory h : histories) {
            messages.add(Map.of("role", h.getRole(), "content", h.getContent()));
        }
        messages.add(Map.of("role", "user", "content", question));

        // 4. 调用大模型
        String answer = llmUtil.chat(messages);

        // 5. 保存本次对话
        ChatHistory userMsg = new ChatHistory();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(question);
        chatHistoryMapper.insert(userMsg);

        ChatHistory assistantMsg = new ChatHistory();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(answer);
        chatHistoryMapper.insert(assistantMsg);

        return answer;
    }
}