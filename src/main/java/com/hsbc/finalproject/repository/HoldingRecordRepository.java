package com.hsbc.finalproject.repository;

import com.hsbc.finalproject.model.HoldingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRecordRepository extends JpaRepository<HoldingRecord,Long> {
    Optional<HoldingRecord> findByUser_IdAndAssetCode(Long userId, String assetCode);

    List<HoldingRecord> findByUser_Id(Long id);
}
