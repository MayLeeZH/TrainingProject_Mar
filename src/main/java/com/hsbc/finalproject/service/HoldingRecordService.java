package com.hsbc.finalproject.service;

import com.hsbc.finalproject.dto.AssetDistributionDTO;
import com.hsbc.finalproject.dto.TotalAssetDTO;
import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.model.HoldingRecord;

import java.util.List;
import java.util.Optional;

public interface HoldingRecordService {
    List<HoldingRecord> showAllHoldingRecords();
    Optional<HoldingRecord> showHoldingRecordById(Long id);
    void deleteHoldingRecordById(Long id);
    HoldingRecord saveHoldingRecord(HoldingRecord holdingRecord);
    HoldingRecord saveHoldingRecordWithTransactions(HoldingRecord holdingRecord);
    TotalAssetDTO getTotalAsset(Long id);

    List<AssetDistributionDTO> getAssetDistribution(Long id);
}
