package com.hsbc.finalproject.Service;


import com.hsbc.finalproject.model.TransactionRecord;

import java.util.List;

public interface TransactionService {
    List<TransactionRecord> listByUser(Long userId);
    TransactionRecord createTransaction(
            TransactionRecord transactionRecord
    );
//    List<TransactionRecord> listByTime();
//    List<TransactionRecord> listByPrice();
//    List<TransactionRecord> listByTotalAmount();
}
