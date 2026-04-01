package com.hsbc.finalproject.controller;

import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.service.YahooFinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/market-data")
public class MarketDataController {

    private final YahooFinanceService yahooFinanceService;

    public MarketDataController(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    @GetMapping("/yahoo/{symbol}")
    public YahooFinanceQuoteResponse getYahooQuote(@PathVariable String symbol) {
        return yahooFinanceService.getQuoteBySymbol(symbol);
    }
}
