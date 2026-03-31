package com.hsbc.finalproject.dto;

public record YahooFinanceQuoteResponse(
        String symbol,
        String shortName,
        String currency,
        String exchangeName,
        String instrumentType,
        Double regularMarketPrice,
        Double previousClose,
        Double priceChange,
        Double priceChangePercent,
        Long regularMarketTime
) {
}
