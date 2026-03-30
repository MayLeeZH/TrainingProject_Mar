package com.project.spring.prototype.service;

import com.project.spring.prototype.model.Portfolio;
import com.project.spring.prototype.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {
    @Autowired
    private PortfolioRepository portfolioRepository;

    public List<Portfolio> getAll() {
        return portfolioRepository.findAll();
    }

    public Optional<Portfolio> getById(Long id) {
        return portfolioRepository.findById(id);
    }

    public void deleteById(Long id) {
        portfolioRepository.deleteById(id);
    }

    public Portfolio save(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }

    public Portfolio update(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }
}
