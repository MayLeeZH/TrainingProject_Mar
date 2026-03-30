package com.project.spring.prototype.controller;

import com.project.spring.prototype.model.Portfolio;
import com.project.spring.prototype.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class PortfolioController {
    @Autowired
    private PortfolioService portfolioService;

    @GetMapping("/portfolios")
    public ResponseEntity<List<Portfolio>> showPortfolios() {
        return ResponseEntity.ok(portfolioService.getAll());
    }

    @GetMapping("/portfolios/{id}")
    public ResponseEntity<Portfolio> showPortfolioById(@PathVariable Long id) {
        Optional<Portfolio> portfolio = portfolioService.getById(id);
        if (portfolio.isPresent()) {
            return ResponseEntity.ok(portfolio.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("portfolios/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {
        if (portfolioService.getById(id).isPresent()) {
            portfolioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/portfolios")
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody Portfolio portfolio) {
        Portfolio savedPortfolio = portfolioService.save(portfolio);
        return new ResponseEntity<>(savedPortfolio, HttpStatus.CREATED);
    }

    @PutMapping("/portfolios/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio portfolio) {
        Optional<Portfolio> curPortfolio = portfolioService.getById(id);
        if (curPortfolio.isPresent()) {
            curPortfolio.get().setName(portfolio.getName());
            portfolioService.save(curPortfolio.get());
            return new ResponseEntity<>(curPortfolio.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
