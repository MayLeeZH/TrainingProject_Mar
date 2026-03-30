package com.project.spring.prototype.controller;

import com.project.spring.prototype.model.Transaction;
import com.project.spring.prototype.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getById(@PathVariable Long id) {
        Optional<Transaction> transaction = transactionService.getById(id);
        return transaction.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction) {
        return new ResponseEntity<>(transactionService.save(transaction), HttpStatus.CREATED);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (transactionService.getById(id).isPresent()) {
            transactionService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id,
                                              @RequestBody Transaction newTx) {

        Optional<Transaction> tx = transactionService.getById(id);

        if (tx.isPresent()) {
            Transaction existing = tx.get();

            existing.setType(newTx.getType());
            existing.setQuantity(newTx.getQuantity());
            existing.setPrice(newTx.getPrice());
            existing.setTimeStamp(newTx.getTimeStamp());
            existing.setPortfolioItem(newTx.getPortfolioItem());

            transactionService.save(existing);

            return ResponseEntity.ok(existing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}