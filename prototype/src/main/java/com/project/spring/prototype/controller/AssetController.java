package com.project.spring.prototype.controller;

import com.project.spring.prototype.model.Asset;
import com.project.spring.prototype.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping("/assets")
    public ResponseEntity<List<Asset>> getAll() {
        return ResponseEntity.ok(assetService.getAll());
    }

    @GetMapping("/assets/{id}")
    public ResponseEntity<Asset> getById(@PathVariable Long id) {
        Optional<Asset> asset = assetService.getById(id);
        return asset.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/assets")
    public ResponseEntity<Asset> create(@RequestBody Asset asset) {
        return new ResponseEntity<>(assetService.save(asset), HttpStatus.CREATED);
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (assetService.getById(id).isPresent()) {
            assetService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<Asset> update(@PathVariable Long id,
                                        @RequestBody Asset newAsset) {

        Optional<Asset> asset = assetService.getById(id);

        if (asset.isPresent()) {
            Asset existing = asset.get();

            existing.setTicker(newAsset.getTicker());
            existing.setName(newAsset.getName());
            existing.setAssetType(newAsset.getAssetType());

            assetService.save(existing);

            return ResponseEntity.ok(existing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}