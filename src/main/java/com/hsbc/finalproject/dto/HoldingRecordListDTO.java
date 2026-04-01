package com.hsbc.finalproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingRecordListDTO {
    private Long id;
    private String assetName;
    private String assetCode;
    private double quantity;
    private double avgPrice;
    private String assetType;
}