package com.hsbc.finalproject.service.Impl;

import com.hsbc.finalproject.dto.HoldingRecordListDTO;
import com.hsbc.finalproject.dto.AssetDistributionDTO;
import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.model.TransactionRecord;
import com.hsbc.finalproject.model.User;
import com.hsbc.finalproject.repository.HoldingRecordRepository;
import com.hsbc.finalproject.repository.UserRepository;
import com.hsbc.finalproject.service.HoldingRecordService;
import jakarta.jws.soap.SOAPBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HoldingRecordServiceImpl implements HoldingRecordService {
    @Autowired
    HoldingRecordRepository recordRepository;
    @Autowired
    YahooFinanceServiceImpl yahooFinanceServiceImpl;
    @Autowired
    UserRepository userRepository;

    @Override
    public List<HoldingRecordListDTO> listHoldingRecordDtos() {
        return recordRepository.findAll().stream()
                .map(record -> new HoldingRecordListDTO(
                        record.getId(),
                        record.getAssetName(),
                        record.getAssetCode(),
                        record.getQuantity(),
                        record.getAvgPrice(),
                        record.getAssetType()
                ))
                .toList();
    }

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
    public List<AssetDistributionDTO> getAssetDistribution(Long id) {
        List<HoldingRecord> holdings = recordRepository.findByUser_Id(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        Map<String, Double> amountMap = new HashMap<>();
        double totalAmount = 0.0;

        for (HoldingRecord record : holdings) {
            // 跳过现金类型的持仓记录（如果数据库中还存有的话）
            if ("CASH".equalsIgnoreCase(record.getAssetType())) {
                continue;
            }

            String type = record.getAssetType();
            double amount = 0.0;

            try {
                // 调用外部API获取当前价格
                YahooFinanceQuoteResponse ret =
                        yahooFinanceServiceImpl.getQuoteBySymbol(record.getAssetCode());
                double currentPrice = ret.regularMarketPrice();
                amount = record.getQuantity() * currentPrice;
            } catch (Exception e) {
                // 行情查不到时，用持仓成本价兜底
                amount = record.getQuantity() * record.getAvgPrice();
            }

            amountMap.merge(type, amount, Double::sum);
            totalAmount += amount;
        }

        double cashAmount = user.getAmount();
        if (cashAmount > 0) {
            amountMap.merge("CASH", cashAmount, Double::sum);
            totalAmount += cashAmount;
        }

        List<AssetDistributionDTO> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : amountMap.entrySet()) {
            AssetDistributionDTO dto = new AssetDistributionDTO();
            dto.setAssetType(entry.getKey());
            dto.setAmount(entry.getValue());

            double proportion = totalAmount == 0 ? 0 : entry.getValue() / totalAmount;
            dto.setProportion(proportion);

            result.add(dto);
        }

        return result;
    }

}
