package com.hsbc.finalproject.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @ManyToOne
    @JsonIgnoreProperties("transactionRecords")
    private User user;

    @ManyToOne
    private HoldingRecord holdingRecord;

}
