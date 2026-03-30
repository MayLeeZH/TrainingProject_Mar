package com.project.spring.prototype.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("portfolio")
    private List<PortfolioItem> items = new ArrayList<PortfolioItem>();

    public Portfolio() {
    }

    public Portfolio(String name, List<PortfolioItem> items) {
        this.name = name;
        this.items = items;
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

    public List<PortfolioItem> getItems() {
        return items;
    }

    public void setItems(List<PortfolioItem> items) {
        this.items = items;
    }
}
