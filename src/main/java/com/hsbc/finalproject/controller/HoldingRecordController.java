package com.hsbc.finalproject.controller;

import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.dto.HoldingRecordListDTO;

import com.hsbc.finalproject.dto.AssetDistributionDTO;
import com.hsbc.finalproject.dto.YahooFinanceQuoteResponse;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.service.HoldingRecordService;
import com.hsbc.finalproject.service.YahooFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ApiResponse<List<HoldingRecord>> showAllHoldingRecords() {
        return new ApiResponse<>(200, "show all success", holdingRecordService.showAllHoldingRecords());
    }

    @GetMapping("/holds/list")
    public List<HoldingRecordListDTO> listHoldingRecords() {
        return holdingRecordService.listHoldingRecordDtos();
    }

    @GetMapping("/holds/{id}")
    public ApiResponse<HoldingRecord> showHoldingRecordById(@PathVariable Long id) {
        Optional<HoldingRecord> holdingRecord = holdingRecordService.showHoldingRecordById(id);
        if (holdingRecord.isPresent()) {
            return new ApiResponse<>(200, "get by id success", holdingRecord.get());
        } else {
            return new ApiResponse<>(400, "user not found", null);
        }
    }

    @GetMapping("/holds/{id}/quote")
    public ApiResponse<YahooFinanceQuoteResponse> showHoldingQuoteById(@PathVariable Long id) {
        Optional<HoldingRecord> holdingRecord = holdingRecordService.showHoldingRecordById(id);
        if (holdingRecord.isPresent()) {
            YahooFinanceQuoteResponse quoteResponse =
                    yahooFinanceService.getQuoteBySymbol(holdingRecord.get().getAssetCode());
            return new ApiResponse<>(200, "querry quote success", quoteResponse);
        } else {
            return new ApiResponse<>(400, "quote not found", null);
        }
    }

    @DeleteMapping("/holds/{id}")
    public ApiResponse<Void> deleteHoldingRecordById(@PathVariable Long id) {
        if (holdingRecordService.showHoldingRecordById(id).isPresent()) {
            holdingRecordService.deleteHoldingRecordById(id);
            return new ApiResponse<>(200, "delete success", null);
        } else {
            return new ApiResponse<>(400, "user not found", null);
        }
    }

    @PostMapping("/holds")
    public ApiResponse<HoldingRecord> addHoldingRecord(@RequestBody HoldingRecord holdingRecord) {
        HoldingRecord saveHoldingRecord = holdingRecordService.saveHoldingRecordWithTransactions(holdingRecord);
        return new ApiResponse<>(200, "add success", saveHoldingRecord);
    }

    
    // Wait for updating
    @PutMapping("/holds/{id}")
    public ApiResponse<HoldingRecord> updateHoldingRecord(@PathVariable Long id, @RequestBody HoldingRecord holdingRecord) {
        Optional<HoldingRecord> optionalHoldingRecord = holdingRecordService.showHoldingRecordById(id);
        if (optionalHoldingRecord.isPresent()) {
            HoldingRecord curHoldingRecord = optionalHoldingRecord.get();
            curHoldingRecord.setAssetName(holdingRecord.getAssetName());
            curHoldingRecord.setAssetCode(holdingRecord.getAssetCode());
            curHoldingRecord.setQuantity(holdingRecord.getQuantity());
            curHoldingRecord.setAvgPrice(holdingRecord.getAvgPrice());
            curHoldingRecord.setAssetType(holdingRecord.getAssetType());
            HoldingRecord updatedHoldingRecord = holdingRecordService.saveHoldingRecord(curHoldingRecord);
            return new ApiResponse<>(200, "update success", updatedHoldingRecord);
        } else {
            return new ApiResponse<>(400, "user not found", null);
        }
    }

    @GetMapping("/holds/asset-distri/{id}")
    public ApiResponse<List<AssetDistributionDTO>> getAssetDistri(@PathVariable Long id){
        return new ApiResponse<>(200,"asset distribution getting successfully",holdingRecordService.getAssetDistribution(id));
    }
}






