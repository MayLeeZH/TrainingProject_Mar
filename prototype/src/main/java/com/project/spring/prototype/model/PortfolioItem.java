package com.project.spring.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class PortfolioItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double quantity;
    private double avgPrice;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonIgnoreProperties("items")
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties("items")
    private Asset asset;

    @OneToMany(mappedBy = "portfolioItem", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<Transaction>();

    public PortfolioItem() {
    }

    public PortfolioItem(double quantity, double avgPrice, Portfolio portfolio, Asset asset, List<Transaction> transactions) {
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.portfolio = portfolio;
        this.asset = asset;
        this.transactions = transactions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
