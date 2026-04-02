package com.hsbc.finalproject.ai;

import com.hsbc.finalproject.model.HoldingRecord;
import com.hsbc.finalproject.model.TransactionRecord;
import com.hsbc.finalproject.model.User;
import com.hsbc.finalproject.repository.HoldingRecordRepository;
import com.hsbc.finalproject.repository.TransactionRecordRepository;
import com.hsbc.finalproject.repository.UserRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PortfolioTools {

    private final HoldingRecordRepository holdingRecordRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final UserRepository userRepository;

    public PortfolioTools(
            HoldingRecordRepository holdingRecordRepository,
            TransactionRecordRepository transactionRecordRepository,
            UserRepository userRepository
    ) {
        this.holdingRecordRepository = holdingRecordRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.userRepository = userRepository;
    }

    @Tool("Get the user's cash balance")
    public String getCashBalance(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return "User not found";
        }

        Double amount = user.get().getAmount();
        return "Cash balance: " + (amount == null ? 0.0 : amount) + " USD";
    }

    @Tool("Get all holdings for a user")
    public String getHoldings(Long userId) {
        List<HoldingRecord> holdings = holdingRecordRepository.findAll().stream()
                .filter(h -> h.getUser() != null && userId.equals(h.getUser().getId()))
                .toList();

        if (holdings.isEmpty()) {
            return "No holdings found for this user";
        }

        return holdings.stream()
                .map(h -> String.format(
                        "%s (%s), type=%s, quantity=%.2f, avgPrice=%.2f",
                        h.getAssetName(),
                        h.getAssetCode(),
                        h.getAssetType(),
                        h.getQuantity(),
                        h.getAvgPrice()
                ))
                .collect(Collectors.joining("\n"));
    }

    @Tool("Get the user's recent transactions")
    public String getRecentTransactions(Long userId) {
        List<TransactionRecord> records = transactionRecordRepository.findByUser_Id(userId);

        if (records.isEmpty()) {
            return "No transactions found for this user";
        }

        return records.stream()
                .limit(10)
                .map(t -> String.format(
                        "%s %s quantity=%.2f price=%.2f at %s",
                        t.getTransactionType(),
                        t.getStockCode(),
                        t.getQuantity(),
                        t.getTransactionalPrice(),
                        t.getTime()
                ))
                .collect(Collectors.joining("\n"));
    }
}
