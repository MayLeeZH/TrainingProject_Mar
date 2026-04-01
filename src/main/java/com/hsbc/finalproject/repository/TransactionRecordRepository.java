package com.hsbc.finalproject.repository;

import com.hsbc.finalproject.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord,Long> {
    List<TransactionRecord> findByUser_Id(Long userId);
}
