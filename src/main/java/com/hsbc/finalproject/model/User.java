package com.hsbc.finalproject.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double amount;
    private String email;

    @OneToMany(mappedBy = "user")
    // 当把 User 序列化出来时，HoldingRecord 里不再返回回 User，避免循环
    @JsonIgnoreProperties("user")
    private List<HoldingRecord> holdingRecords;

    @OneToMany(mappedBy = "user")
    // 当把 User 序列化出来时，TransactionRecord 里不再返回回 User，避免循环
    @JsonIgnoreProperties("user")
    private List<TransactionRecord> transactionRecords;

}
