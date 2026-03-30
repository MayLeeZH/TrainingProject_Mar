package com.project.spring.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private String name;

    @Enumerated(EnumType.STRING)
    private AssetType assetType;

    @OneToMany(mappedBy = "asset")
    @JsonIgnoreProperties("asset")
    private List<PortfolioItem> items = new ArrayList<PortfolioItem>();

    public Asset() {
    }

    public Asset(String ticker, String name, List<PortfolioItem> items, AssetType assetType) {
        this.ticker = ticker;
        this.name = name;
        this.items = items;
        this.assetType = assetType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public List<PortfolioItem> getItems() {
        return items;
    }

    public void setItems(List<PortfolioItem> items) {
        this.items = items;
    }
}
