package com.hsbc.finalproject.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private double quantity;

    private LocalDateTime time;

    private double transactionalPrice;

    @ManyToOne
    @JsonIgnoreProperties("transactionRecords")
    private User user;

    @ManyToOne
    private HoldingRecord holdingRecord;

    public TransactionRecord(TransactionType transactionType, double quantity, LocalDateTime time, double transactionalPrice, User user, HoldingRecord holdingRecord) {
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.time = time;
        this.transactionalPrice = transactionalPrice;
        this.user = user;
        this.holdingRecord = holdingRecord;
    }

    public TransactionRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public double getTransactionalPrice() {
        return transactionalPrice;
    }

    public void setTransactionalPrice(double transactionalPrice) {
        this.transactionalPrice = transactionalPrice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HoldingRecord getHoldingRecord() {
        return holdingRecord;
    }

    public void setHoldingRecord(HoldingRecord holdingRecord) {
        this.holdingRecord = holdingRecord;
    }
}
