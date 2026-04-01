package com.hsbc.finalproject.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hsbc.finalproject.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private double quantity;

    private LocalDateTime time;

    private double transactionalPrice;

    // 新增股票名称
    private String stockName;

    // 新增股票代码
    private String stockCode;

    @ManyToOne
    // 防止 TransactionRecord -> User -> (holdingRecords/transactionRecords) 的递归
    @JsonIgnoreProperties({"holdingRecords", "transactionRecords"})
    private User user;

    @ManyToOne
    // 防止 TransactionRecord -> holdingRecord -> user/transactionRecords 再次回环
    @JsonIgnoreProperties({"transactionRecords", "user"})
    private HoldingRecord holdingRecord;

}
