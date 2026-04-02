package com.hsbc.finalproject.service.Impl;

import com.hsbc.finalproject.ai.PortfolioAssistant;
import com.hsbc.finalproject.ai.PortfolioTools;
import com.hsbc.finalproject.service.ChatService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final PortfolioAssistant assistant;

    public ChatServiceImpl(ChatModel chatModel, PortfolioTools portfolioTools) {
        this.assistant = AiServices.builder(PortfolioAssistant.class)
                .chatModel(chatModel)
                .tools(portfolioTools)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    @Override
    public String chat(Long userId, String message) {
        return assistant.chat(userId, message);
    }
}
