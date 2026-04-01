package com.hsbc.finalproject.service.Impl;

import com.hsbc.finalproject.dto.HoldingRecordListDTO;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.model.TransactionRecord;
import com.hsbc.finalproject.repository.HoldingRecordRepository;
import com.hsbc.finalproject.service.HoldingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HoldingRecordServiceImpl implements HoldingRecordService {
    @Autowired
    HoldingRecordRepository recordRepository;

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

}
