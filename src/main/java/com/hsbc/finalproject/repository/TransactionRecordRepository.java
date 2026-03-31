package com.hsbc.finalproject.repository;

import com.hsbc.finalproject.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord,Long> {
}
