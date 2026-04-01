package com.hsbc.finalproject.service.Impl;

import com.hsbc.finalproject.model.enums.AssetType;
import com.hsbc.finalproject.model.enums.TransactionType;
import com.hsbc.finalproject.repository.UserRepository;
import com.hsbc.finalproject.service.TransactionService;
import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.model.TransactionRecord;
import com.hsbc.finalproject.model.User;
import com.hsbc.finalproject.repository.HoldingRecordRepository;
import com.hsbc.finalproject.repository.TransactionRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

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
        log.info("========== 开始处理交易请求 ==========");
        log.info("请求数据: stockCode={}, stockName={}, stockType={}, transactionType={}, quantity={}, price={}",
                request.getStockCode(), request.getStockName(), request.getStockType(),
                request.getTransactionType(), request.getQuantity(), request.getTransactionalPrice());

        // 1. 校验请求和用户
        if (request == null || request.getUser() == null || request.getUser().getId() == null) {
            log.error("校验失败: 用户信息为空");
            return new ApiResponse<>(400, "need provide user", null);
        }

        // 2. 直接从请求中获取股票信息（不需要从 holdingRecord 查）
        String stockCode = request.getStockCode();
        String stockName = request.getStockName();
        AssetType stockType = request.getStockType();
        Double quantity = request.getQuantity();
        Double transactionalPrice = request.getTransactionalPrice();
        TransactionType transactionType = request.getTransactionType();

        // 3. 校验必填字段
        if (stockCode == null || stockCode.isEmpty()) {
            return new ApiResponse<>(400, "stockCode is required", null);
        }
        if (stockName == null || stockName.isEmpty()) {
            return new ApiResponse<>(400, "stockName is required", null);
        }
        if (quantity == null || quantity <= 0) {
            return new ApiResponse<>(400, "quantity must be > 0", null);
        }
        if (transactionalPrice == null || transactionalPrice <= 0) {
            return new ApiResponse<>(400, "transactionalPrice must be > 0", null);
        }
        if (transactionType == null) {
            return new ApiResponse<>(400, "transactionType is required", null);
        }

        // 4. 验证用户是否存在
        Optional<User> optionalUser = userRepository.findById(request.getUser().getId());
        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(400, "user not found", null);
        }
        User user = optionalUser.get();

        // 5. 查找或创建持仓记录
        log.info("查找持仓: userId={}, stockCode={}", user.getId(), stockCode);
        Optional<HoldingRecord> existingHolding =
                holdingRecordRepository.findByUser_IdAndAssetCode(user.getId(), stockCode);

        HoldingRecord holdingRecord = existingHolding.orElseGet(HoldingRecord::new);
        boolean isNewHolding = holdingRecord.getId() == null;

        if (isNewHolding) {
            log.info("未找到持仓，创建新持仓");
            // 新持仓初始化
            holdingRecord.setUser(user);
            holdingRecord.setAssetCode(stockCode);
            holdingRecord.setAssetName(stockName);
            holdingRecord.setAssetType(stockType);
            holdingRecord.setQuantity(0.0);
            holdingRecord.setAvgPrice(0.0);
        } else {
            log.info("找到已有持仓: id={}, 当前数量={}, 当前均价={}", holdingRecord.getId(), holdingRecord.getQuantity(), holdingRecord.getAvgPrice());
        }

        // 6. 根据交易类型更新持仓
        if (transactionType == TransactionType.BUY) {
            // 买入：累加数量，重新计算均价
            double oldQty = holdingRecord.getQuantity();
            double oldAvg = holdingRecord.getAvgPrice();
            double newQty = oldQty + quantity;
            double newAvg = ((oldQty * oldAvg) + (quantity * transactionalPrice)) / newQty;

            log.info("【买入】更新持仓: 数量 {} + {} = {}, 均价 {} -> {}",
                    oldQty, quantity, newQty, oldAvg, newAvg);

            holdingRecord.setQuantity(newQty);
            holdingRecord.setAvgPrice(newAvg);
        } else {
            // 卖出：检查数量是否足够
            double oldQty = holdingRecord.getQuantity();
            if (oldQty < quantity) {
                log.error("【卖出】持仓不足: 当前持有={}, 想要卖出={}", oldQty, quantity);
                return new ApiResponse<>(400, "持仓数量不足，当前持有: " + oldQty + "，无法卖出: " + quantity, null);
            }

            double newQty = oldQty - quantity;
            log.info("【卖出】更新持仓: 数量 {} - {} = {}", oldQty, quantity, newQty);

            holdingRecord.setQuantity(newQty);
            // 如果卖光了，清除均价
            if (newQty == 0) {
                holdingRecord.setAvgPrice(0.0);
            }
        }

        // 7. 保存持仓记录
        HoldingRecord savedHolding = holdingRecordRepository.save(holdingRecord);
        log.info("持仓已保存: id={}", savedHolding.getId());

        // 8. 创建交易记录
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setTransactionType(transactionType);
        transactionRecord.setQuantity(quantity);
        transactionRecord.setTransactionalPrice(transactionalPrice);
        transactionRecord.setStockCode(stockCode);
        transactionRecord.setStockName(stockName);
        transactionRecord.setStockType(stockType);
        transactionRecord.setTime(request.getTime() != null ? request.getTime() : LocalDateTime.now());
        transactionRecord.setUser(user);
        transactionRecord.setHoldingRecord(savedHolding);

        TransactionRecord saved = transactionRecordRepository.save(transactionRecord);
        log.info("交易记录已保存: id={}", saved.getId());
        log.info("========== 交易处理完成 ==========");

        return new ApiResponse<>(200, "交易创建成功", saved);
    }
}
