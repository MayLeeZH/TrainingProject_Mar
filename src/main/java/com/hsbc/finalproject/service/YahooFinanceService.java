package com.hsbc.finalproject.service;

import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;

public interface YahooFinanceService {
    YahooFinanceQuoteResponse getQuoteBySymbol(String symbol);
}
