package com.hsbc.finalproject.Controller;

import com.hsbc.finalproject.Service.TransactionService;
import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.model.TransactionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

//    列出某个用户的交易记录
    @GetMapping("/users/{userId}")
    public List<TransactionRecord> listByUser(@PathVariable Long userId) {
        return transactionService.listByUser(userId);
    }

    @PostMapping
    public ApiResponse<TransactionRecord> createTransaction(@RequestBody TransactionRecord transactionRecord) {
        return transactionService.createTransaction(transactionRecord);
    }
}
