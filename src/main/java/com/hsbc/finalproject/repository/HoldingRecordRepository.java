package com.hsbc.finalproject.repository;

import com.hsbc.finalproject.model.HoldingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRecordRepository extends JpaRepository<HoldingRecord,Long> {
}
