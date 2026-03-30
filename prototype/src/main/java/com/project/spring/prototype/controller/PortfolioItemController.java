package com.project.spring.prototype.controller;

import com.project.spring.prototype.model.PortfolioItem;
import com.project.spring.prototype.service.PortfolioItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class PortfolioItemController {

    @Autowired
    private PortfolioItemService portfolioItemService;

    @GetMapping("/items")
    public ResponseEntity<List<PortfolioItem>> getAll() {
        return ResponseEntity.ok(portfolioItemService.getAll());
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<PortfolioItem> getById(@PathVariable Long id) {
        Optional<PortfolioItem> item = portfolioItemService.getById(id);
        return item.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<PortfolioItem> create(@RequestBody PortfolioItem item) {
        return new ResponseEntity<>(portfolioItemService.save(item), HttpStatus.CREATED);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (portfolioItemService.getById(id).isPresent()) {
            portfolioItemService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<PortfolioItem> update(@PathVariable Long id, @RequestBody PortfolioItem newItem) {
        Optional<PortfolioItem> item = portfolioItemService.getById(id);

        if (item.isPresent()) {
            PortfolioItem existing = item.get();
            existing.setQuantity(newItem.getQuantity());
            existing.setAvgPrice(newItem.getAvgPrice());
            existing.setAsset(newItem.getAsset());
            existing.setPortfolio(newItem.getPortfolio());
            portfolioItemService.save(existing);

            return ResponseEntity.ok(existing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}