package com.hsbc.finalproject.exception;

public class InsufficientHoldingsException extends RuntimeException {
    
    public InsufficientHoldingsException(String message) {
        super(message);
    }
    
    public InsufficientHoldingsException(double available, double requested) {
        super(String.format("Insufficient holdings: you have %.2f, but trying to sell %.2f", available, requested));
    }
}
