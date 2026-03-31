package com.hsbc.finalproject.service;


import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.model.TransactionRecord;

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
