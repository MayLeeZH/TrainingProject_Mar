package com.hsbc.finalproject.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class HoldingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetName;
    private String assetCode;
    private double quantity;
    private double avgPrice;
    private String assetType;

    @ManyToOne
    @JsonIgnoreProperties("holdingRecords")
    private User user;

    @OneToMany(mappedBy = "holdingRecord")
    @JsonIgnoreProperties("holdingRecord")
    private List<TransactionRecord> transactionRecords;

    public HoldingRecord(String assetName, String assetCode, double quantity, double avgPrice, String assetType, User user, List<TransactionRecord> transactionRecords) {
        this.assetName = assetName;
        this.assetCode = assetCode;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.assetType = assetType;
        this.user = user;
        this.transactionRecords = transactionRecords;
    }

    public HoldingRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TransactionRecord> getTransactionRecords() {
        return transactionRecords;
    }

    public void setTransactionRecords(List<TransactionRecord> transactionRecords) {
        this.transactionRecords = transactionRecords;
    }
}
