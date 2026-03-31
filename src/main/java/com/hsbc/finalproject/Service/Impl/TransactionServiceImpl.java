package com.hsbc.finalproject.Service.Impl;

import com.hsbc.finalproject.Service.TransactionService;
import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.model.TransactionRecord;
import com.hsbc.finalproject.model.TransactionType;
import com.hsbc.finalproject.model.User;
import com.hsbc.finalproject.repository.HoldingRecordRepository;
import com.hsbc.finalproject.repository.TransactionRecordRepository;
import com.hsbc.finalproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.hsbc.finalproject.model.TransactionType.BUY;
import static com.hsbc.finalproject.model.TransactionType.SELL;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;
    @Autowired
    private HoldingRecordRepository holdingRecordRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<TransactionRecord> listByUser(Long userId) {
        return transactionRecordRepository.findByUser_Id(userId);
    }

    @Override
    @Transactional
    public ApiResponse<TransactionRecord> createTransaction(TransactionRecord request) {
        if (request == null || request.getUser() == null || request.getUser().getId() == null) {
            return new ApiResponse<>(400,"need provide user",null);
        }

        //买入的时候可能没有holding
        if (request.getTransactionType()==SELL &&(request.getHoldingRecord() == null || request.getHoldingRecord().getAssetCode() == null)) {
            return new ApiResponse<>(400,"need provide holding record to sell",null);
        }

        Long userId = request.getUser().getId();
        String assetCode = request.getHoldingRecord().getAssetCode();
        String assetName = request.getHoldingRecord().getAssetName();
        String assetType = request.getHoldingRecord().getAssetType();
        double quantity = request.getQuantity();
        double transactionalPrice = request.getTransactionalPrice();
        TransactionType transactionType = request.getTransactionType();

        if (quantity <= 0 || transactionalPrice <= 0) {
            return new ApiResponse<>(400,"quantity and price must be > 0",null);
        }
        if (transactionType == null) {
            return new ApiResponse<>(400,"transactionType is required",null);
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(400,"user not found",null);
        }
        User user = optionalUser.get();

        Optional<HoldingRecord> existingHolding =
                holdingRecordRepository.findByUser_IdAndAssetCode(userId, assetCode);

        HoldingRecord holdingRecord = existingHolding.orElseGet(HoldingRecord::new);
        if (holdingRecord.getId() == null) {
            holdingRecord.setUser(user);
            holdingRecord.setAssetCode(assetCode);
            holdingRecord.setAssetName(assetName);
            holdingRecord.setAssetType(assetType);
            holdingRecord.setQuantity(0);
            holdingRecord.setAvgPrice(0);
        }

//        double amountDelta = quantity * transactionalPrice;
        if (transactionType == BUY) {
            double oldQty = holdingRecord.getQuantity();
            double oldAvg = holdingRecord.getAvgPrice();
            double newQty = oldQty + quantity;
            double newAvg = ((oldQty * oldAvg) + (quantity * transactionalPrice)) / newQty;

//            user.setAmount(user.getAmount() + amountDelta);
            holdingRecord.setQuantity(newQty);
            holdingRecord.setAvgPrice(newAvg);
        } else {

            double oldQty = holdingRecord.getQuantity();
            if (oldQty < quantity) {
                new ApiResponse<>(400,"not enough holding quantity" + quantity,null);
            }


            double newQty = oldQty - quantity;
            double oldAvg = holdingRecord.getAvgPrice();
            if (newQty == 0) {
                holdingRecord.setAvgPrice(0);
            }
            double newAvg = ((oldQty * oldAvg) - (quantity * transactionalPrice)) / newQty;
//            user.setAmount(user.getAmount() + amountDelta);
            holdingRecord.setQuantity(newQty);
            holdingRecord.setAvgPrice(newAvg);
        }

        userRepository.save(user);
        HoldingRecord savedHolding = holdingRecordRepository.save(holdingRecord);

        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setTransactionType(transactionType);
        transactionRecord.setQuantity(quantity);
        transactionRecord.setTransactionalPrice(transactionalPrice);
        transactionRecord.setTime(request.getTime() != null ? request.getTime() : LocalDateTime.now());
        transactionRecord.setUser(user);
        transactionRecord.setHoldingRecord(savedHolding);
        transactionRecordRepository.save(transactionRecord);

        return new ApiResponse<>(200,"success",transactionRecord);
    }
}
