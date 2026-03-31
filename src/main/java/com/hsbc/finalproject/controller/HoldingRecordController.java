package com.hsbc.finalproject.controller;

import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.service.HoldingRecordService;
import com.hsbc.finalproject.service.YahooFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class HoldingRecordController {
    @Autowired
    private HoldingRecordService holdingRecordService;

    @Autowired
    private YahooFinanceService yahooFinanceService;

    @GetMapping("/holds")
    public ResponseEntity<List<HoldingRecord>> showAllHoldingRecords() {
        return ResponseEntity.ok(holdingRecordService.showAllHoldingRecords());
    }

    @GetMapping("/holds/{id}")
    public ResponseEntity<HoldingRecord> showHoldingRecordById(@PathVariable Long id) {
        Optional<HoldingRecord> holdingRecord = holdingRecordService.showHoldingRecordById(id);
        if (holdingRecord.isPresent()) {
            return ResponseEntity.ok(holdingRecord.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/holds/{id}/quote")
    public ResponseEntity<YahooFinanceQuoteResponse> showHoldingQuoteById(@PathVariable Long id) {
        Optional<HoldingRecord> holdingRecord = holdingRecordService.showHoldingRecordById(id);
        if (holdingRecord.isPresent()) {
            YahooFinanceQuoteResponse quoteResponse =
                    yahooFinanceService.getQuoteBySymbol(holdingRecord.get().getAssetCode());
            return ResponseEntity.ok(quoteResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/holds/{id}")
    public ResponseEntity<HoldingRecord> deleteHoldingRecordById(@PathVariable Long id) {
        if (holdingRecordService.showHoldingRecordById(id).isPresent()) {
            holdingRecordService.deleteHoldingRecordById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/holds")
    public ResponseEntity<HoldingRecord> addHoldingRecord(@RequestBody HoldingRecord holdingRecord) {
        HoldingRecord saveHoldingRecord = holdingRecordService.saveHoldingRecordWithTransactions(holdingRecord);
        return new ResponseEntity<>(saveHoldingRecord, HttpStatus.CREATED);
    }

    
    // Wait for updating
    @PutMapping("/holds/{id}")
    public ResponseEntity<HoldingRecord> updateHoldingRecord(@PathVariable Long id, @RequestBody HoldingRecord holdingRecord) {
        Optional<HoldingRecord> optionalHoldingRecord = holdingRecordService.showHoldingRecordById(id);
        if (optionalHoldingRecord.isPresent()) {
            HoldingRecord curHoldingRecord = optionalHoldingRecord.get();
            curHoldingRecord.setAssetName(holdingRecord.getAssetName());
            curHoldingRecord.setAssetCode(holdingRecord.getAssetCode());
            curHoldingRecord.setQuantity(holdingRecord.getQuantity());
            curHoldingRecord.setAvgPrice(holdingRecord.getAvgPrice());
            curHoldingRecord.setAssetType(holdingRecord.getAssetType());
            HoldingRecord updatedHoldingRecord = holdingRecordService.saveHoldingRecord(curHoldingRecord);
            return ResponseEntity.ok(updatedHoldingRecord);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}






