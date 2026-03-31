package com.hsbc.finalproject.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.service.YahooFinanceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class YahooFinanceServiceImpl implements YahooFinanceService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YahooFinanceServiceImpl(
            @Value("${yahoo.finance.base-url:https://query1.finance.yahoo.com}") String yahooFinanceBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(yahooFinanceBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(
                        HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                + "(KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
                )
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public YahooFinanceQuoteResponse getQuoteBySymbol(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        String responseBody;

        try {
            responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v8/finance/chart/{symbol}")
                            .queryParam("interval", "1d")
                            .queryParam("range", "1d")
                            .build(normalizedSymbol))
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Yahoo Finance returned " + exception.getStatusCode().value()
                            + " for symbol " + normalizedSymbol
                            + ": " + trimResponseBody(exception.getResponseBodyAsString()),
                    exception
            );
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to call Yahoo Finance for symbol " + normalizedSymbol,
                    exception
            );
        }

        return parseQuote(responseBody, normalizedSymbol);
    }

    private YahooFinanceQuoteResponse parseQuote(String responseBody, String symbol) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultNode = root.path("chart").path("result");

            if (!resultNode.isArray() || resultNode.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No market data returned by Yahoo Finance for symbol " + symbol
                );
            }

            JsonNode metaNode = resultNode.get(0).path("meta");
            if (metaNode.isMissingNode() || metaNode.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Yahoo Finance response does not contain quote metadata for symbol " + symbol
                );
            }

            Double regularMarketPrice = getNullableDouble(metaNode, "regularMarketPrice");
            Double previousClose = getNullableDouble(metaNode, "previousClose");
            Double priceChange = calculatePriceChange(regularMarketPrice, previousClose);
            Double priceChangePercent = calculatePriceChangePercent(priceChange, previousClose);

            return new YahooFinanceQuoteResponse(
                    getNullableText(metaNode, "symbol"),
                    getNullableText(metaNode, "shortName"),
                    getNullableText(metaNode, "currency"),
                    getNullableText(metaNode, "exchangeName"),
                    getNullableText(metaNode, "instrumentType"),
                    regularMarketPrice,
                    previousClose,
                    priceChange,
                    priceChangePercent,
                    getNullableLong(metaNode, "regularMarketTime")
            );
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to parse Yahoo Finance response for symbol " + symbol,
                    exception
            );
        }
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol must not be blank");
        }
        return symbol.trim().toUpperCase();
    }

    private Double calculatePriceChange(Double regularMarketPrice, Double previousClose) {
        if (regularMarketPrice == null || previousClose == null) {
            return null;
        }
        return regularMarketPrice - previousClose;
    }

    private Double calculatePriceChangePercent(Double priceChange, Double previousClose) {
        if (priceChange == null || previousClose == null || previousClose == 0) {
            return null;
        }
        return priceChange / previousClose * 100;
    }

    private String getNullableText(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        String value = fieldNode.asText();
        return value.isBlank() ? null : value;
    }

    private Double getNullableDouble(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull() || !fieldNode.isNumber()) {
            return null;
        }
        return fieldNode.asDouble();
    }

    private Long getNullableLong(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull() || !fieldNode.isNumber()) {
            return null;
        }
        return fieldNode.asLong();
    }

    private String trimResponseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty response body";
        }
        String normalized = responseBody.replaceAll("\\s+", " ").trim();
        return normalized.length() > 200 ? normalized.substring(0, 200) + "..." : normalized;
    }
}
