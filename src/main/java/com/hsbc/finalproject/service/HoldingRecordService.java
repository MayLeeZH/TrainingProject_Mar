package com.hsbc.finalproject.service;

import com.hsbc.finalproject.model.HoldingRecord;

import java.util.List;
import java.util.Optional;

public interface HoldingRecordService {
    List<HoldingRecord> showAllHoldingRecords();
    Optional<HoldingRecord> showHoldingRecordById(Long id);
    void deleteHoldingRecordById(Long id);
    HoldingRecord saveHoldingRecord(HoldingRecord holdingRecord);
    HoldingRecord saveHoldingRecordWithTransactions(HoldingRecord holdingRecord);
}
