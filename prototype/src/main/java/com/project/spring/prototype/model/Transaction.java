package com.project.spring.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private double quantity;
    private double price;
    private LocalDateTime timeStamp;

    @ManyToOne
    @JoinColumn(name = "portfolio_item_id")
    @JsonIgnoreProperties("transactions")
    private PortfolioItem portfolioItem;

    public Transaction() {
    }

    public Transaction(TransactionType type, double quantity, double price, LocalDateTime timeStamp, PortfolioItem portfolioItem) {
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.timeStamp = timeStamp;
        this.portfolioItem = portfolioItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public PortfolioItem getPortfolioItem() {
        return portfolioItem;
    }

    public void setPortfolioItem(PortfolioItem portfolioItem) {
        this.portfolioItem = portfolioItem;
    }
}
