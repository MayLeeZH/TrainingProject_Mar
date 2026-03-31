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
    private double quantity;
    private double avgPrice;
    private String assetType;

    @ManyToOne
    @JsonIgnoreProperties("holdingRecords")
    private User user;

    @OneToMany(mappedBy = "holdingRecord")
    @JsonIgnoreProperties("holdingRecord")
    private List<TransactionRecord> transactionRecords;

}
