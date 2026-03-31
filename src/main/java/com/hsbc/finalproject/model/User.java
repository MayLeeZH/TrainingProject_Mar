package com.hsbc.finalproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double amount;
    private String email;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<HoldingRecord> holdingRecords;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private List<TransactionRecord> transactionRecords;

    public User(String name, double amount, String email, List<HoldingRecord> holdingRecords, List<TransactionRecord> transactionRecords) {
        this.name = name;
        this.amount = amount;
        this.email = email;
        this.holdingRecords = holdingRecords;
        this.transactionRecords = transactionRecords;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<HoldingRecord> getHoldingRecords() {
        return holdingRecords;
    }

    public void setHoldingRecords(List<HoldingRecord> holdingRecords) {
        this.holdingRecords = holdingRecords;
    }

    public List<TransactionRecord> getTransactionRecords() {
        return transactionRecords;
    }

    public void setTransactionRecords(List<TransactionRecord> transactionRecords) {
        this.transactionRecords = transactionRecords;
    }
}
