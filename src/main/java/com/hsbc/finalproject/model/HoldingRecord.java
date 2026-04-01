package com.hsbc.finalproject.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetName;
    private String assetCode;
    private Double quantity;
    private Double avgPrice;
    private String assetType;

    @ManyToOne
    // 防止 HoldingRecord -> User -> HoldingRecord/TransactionRecord 的递归序列化
    @JsonIgnoreProperties({"holdingRecords", "transactionRecords"})
    private User user;

    @OneToMany(mappedBy = "holdingRecord", cascade = CascadeType.ALL)
    // 当返回 HoldingRecord 时，避免继续序列化回到 HoldingRecord 或把 user 的完整图拉出来
    @JsonIgnoreProperties({"holdingRecord", "user"})
    private List<TransactionRecord> transactionRecords;

}
