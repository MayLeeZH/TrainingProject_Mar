package com.hsbc.finalproject.Service;


import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.model.TransactionRecord;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TransactionService {
    List<TransactionRecord> listByUser(Long userId);
    ApiResponse<TransactionRecord> createTransaction(
            TransactionRecord transactionRecord
    );
//    List<TransactionRecord> listByTime();
//    List<TransactionRecord> listByPrice();
//    List<TransactionRecord> listByTotalAmount();
}
