package com.project.spring.prototype.service;

import com.project.spring.prototype.model.PortfolioItem;
import com.project.spring.prototype.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PortfolioItemService {

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    public List<PortfolioItem> getAll() {
        return portfolioItemRepository.findAll();
    }

    public Optional<PortfolioItem> getById(Long id) {
        return portfolioItemRepository.findById(id);
    }

    public void deleteById(Long id) {
        portfolioItemRepository.deleteById(id);
    }

    public PortfolioItem save(PortfolioItem item) {
        return portfolioItemRepository.save(item);
    }
}