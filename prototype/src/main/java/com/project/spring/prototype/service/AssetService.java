package com.project.spring.prototype.service;

import com.project.spring.prototype.model.Asset;
import com.project.spring.prototype.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    public List<Asset> getAll() {
        return assetRepository.findAll();
    }

    public Optional<Asset> getById(Long id) {
        return assetRepository.findById(id);
    }

    public Optional<Asset> getByTicker(String ticker) {
        return assetRepository.findByTicker(ticker);
    }

    public void deleteById(Long id) {
        assetRepository.deleteById(id);
    }

    public Asset save(Asset asset) {
        return assetRepository.save(asset);
    }
}