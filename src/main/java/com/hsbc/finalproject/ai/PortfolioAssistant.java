package com.hsbc.finalproject.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PortfolioAssistant {

    @SystemMessage("""
        You are a portfolio assistant for an investment dashboard.
        Be concise, accurate, and helpful.
        Use tools when the user asks about holdings, transactions, or cash balance.
        Do not invent data.
        If data is unavailable, say so clearly.
        """)
    String chat(@V("userId") Long userId, @UserMessage String message);
}
