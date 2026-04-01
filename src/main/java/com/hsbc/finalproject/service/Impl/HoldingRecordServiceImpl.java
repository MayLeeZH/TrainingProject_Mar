package com.hsbc.finalproject.service.Impl;

import com.hsbc.finalproject.dto.AssetDistributionDTO;
import com.hsbc.finalproject.dto.TotalAssetDTO;
import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.model.TransactionRecord;
import com.hsbc.finalproject.model.enums.AssetType;
import com.hsbc.finalproject.repository.HoldingRecordRepository;
import com.hsbc.finalproject.service.HoldingRecordService;
import com.hsbc.finalproject.service.YahooFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HoldingRecordServiceImpl implements HoldingRecordService {
    @Autowired
    HoldingRecordRepository recordRepository;
    @Autowired
    YahooFinanceService yahooFinanceService;

    @Override
    public List<HoldingRecord> showAllHoldingRecords() {
        return recordRepository.findAll();
    }

    @Override
    public Optional<HoldingRecord> showHoldingRecordById(Long id) {
        return recordRepository.findById(id);
    }

    @Override
    public void deleteHoldingRecordById(Long id) {
        recordRepository.deleteById(id);
    }

    @Override
    public HoldingRecord saveHoldingRecord(HoldingRecord holdingRecord) {
        return recordRepository.save(holdingRecord);
    }

    @Override
    public HoldingRecord saveHoldingRecordWithTransactions(HoldingRecord holdingRecord) {
        if (holdingRecord.getTransactionRecords() != null) {
            for (TransactionRecord transactionRecord : holdingRecord.getTransactionRecords()) {
                transactionRecord.setHoldingRecord(holdingRecord);
            }
        }
        return recordRepository.save(holdingRecord);
    }

    @Override
    public TotalAssetDTO getTotalAsset(Long id) {

        return null;
    }

    @Override
    public List<AssetDistributionDTO> getAssetDistribution(Long id) {
        List<HoldingRecord> holdings = recordRepository.findByUser_Id(id);

        Map<AssetType, Double> amountMap = new HashMap<>();
        double totalAmount = 0.0;

        for (HoldingRecord record : holdings) {
            AssetType type = record.getAssetType();
            double amount;

            if (type == AssetType.Cash) {
                amount = record.getQuantity();
            } else {
                // 其他资产：调用外部API获取当前价格
                YahooFinanceQuoteResponse ret = yahooFinanceService.getQuoteBySymbol(record.getAssetCode());
                double currentPrice = ret.regularMarketPrice();
                amount = record.getQuantity() * currentPrice;
            }

            // 累加该类型的金额
            amountMap.merge(type, amount, Double::sum);
            totalAmount += amount;
        }
        // 3. 构建DTO列表并计算比例
        List<AssetDistributionDTO> result = new ArrayList<>();
        for (Map.Entry<AssetType, Double> entry : amountMap.entrySet()) {
            AssetDistributionDTO dto = new AssetDistributionDTO();
            dto.setAssetType(entry.getKey());
            dto.setAmount(entry.getValue());
            // 计算比例（避免除零）
            double proportion = totalAmount == 0 ? 0 : (entry.getValue() / totalAmount);
            dto.setProportion(proportion);
            result.add(dto);
        }
        return result;
    }

}
