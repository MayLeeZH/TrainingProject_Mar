package com.hsbc.finalproject.controller;

import com.hsbc.finalproject.dto.ChatRequest;
import com.hsbc.finalproject.dto.ChatResponse;
import com.hsbc.finalproject.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = chatService.chat(request.getUserId(), request.getMessage());
        return new ChatResponse(reply);
    }
}
