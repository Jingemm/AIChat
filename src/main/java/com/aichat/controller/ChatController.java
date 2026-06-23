package com.aichat.controller;

import com.aichat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping
    public String chat(@RequestParam String sessionId, @RequestParam String question) {
        return chatService.chat(sessionId, question);
    }
}